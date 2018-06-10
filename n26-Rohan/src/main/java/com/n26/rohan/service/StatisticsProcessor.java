package com.n26.rohan.service;

import static com.n26.rohan.service.Helper.getElapsedDuration;
import static com.n26.rohan.service.Helper.roundDownToSeconds;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.n26.rohan.model.StatisticsData;
import com.n26.rohan.model.Transaction;

public class StatisticsProcessor {

	/**
	 * The interval amount in milliseconds for statistics window
	 */
	public static final int OVERALL_INTERVAL = 60 * 1000;
	
	/**
	 * Map for the statistics data for each second (only keep data for last 60
	 * seconds) .
	 */
	private final ConcurrentNavigableMap<Long, StatisticsData> perodicStatsMap;

	/**
	 * Overall Statistical data for the application, and statistics service
	 * return it directly.
	 */
	private StatisticsData overallStats;

	/**
	 * To keep the consistency of overallStats values, we write updates at
	 * draftOverallStats and when update process finish we copy the changed data
	 * to overallStats (same as transaction and commit concept).
	 */
	private StatisticsData draftOverallStats;

	/**
	 * Used to prevent concurrent update at draftOverallStats, we can use
	 * draftOverallStats as locker because we change the object reference at
	 * commit method.
	 */
	private final Object overallStatsLock;
	

	public StatisticsProcessor() {
		this.perodicStatsMap = new ConcurrentSkipListMap<Long, StatisticsData>();
		this.overallStats = new StatisticsData();
		this.draftOverallStats = new StatisticsData();
		this.overallStatsLock = new Object();
	}

	public void processTransaction(Transaction transaction) {
		long timestampInSeconds = roundDownToSeconds(transaction.getTimestamp());
		// only handle the transaction if it is not older than overall duration (60 seconds)
		// TODO current logic not handle it the timestamp in the future as this will make the code more complicated and this wasn't defined in task scope
		if (getElapsedDuration(timestampInSeconds) <= OVERALL_INTERVAL) {
			StatisticsData perodicStats = perodicStatsMap.get(timestampInSeconds);
			if (perodicStats == null) {
				perodicStats = new StatisticsData();
				perodicStatsMap.put(timestampInSeconds, perodicStats);
			}
			addAmountToStats(perodicStats, transaction.getAmount());
			synchronized (overallStatsLock) {
				addAmountToStats(draftOverallStats, transaction.getAmount());
				commitOverallStatsChanges();
			}
		}
	}
	
	public void slidePerodicStatsicsWindow() {
		Entry<Long, StatisticsData> firstEntry = perodicStatsMap.firstEntry();
		if(firstEntry!=null){
			Long minPerodicTimestamp = firstEntry.getKey();
			if (getElapsedDuration(minPerodicTimestamp) > OVERALL_INTERVAL) {
				removeFromPerodicStats(minPerodicTimestamp);
			}
		}
	}
	
	public StatisticsData getOverallStatistics(){
		return overallStats;
	}
	
	
	public long getOverallInterval() {
		return OVERALL_INTERVAL;
	}

	private void addAmountToStats(StatisticsData statsData, double amount) {
		statsData.setSum(statsData.getSum() + amount);
		statsData.setCount(statsData.getCount() + 1);
		statsData.setAvg(Helper.divide(statsData.getSum(), statsData.getCount()));
		if (amount > statsData.getMax()) {
			statsData.setMax(amount);
		}
		if (statsData.getCount() == 1 || amount < statsData.getMin()) {
			statsData.setMin(amount);
		}
	}

	// apply draftOverallStats changes to overallStatsLock
	private void commitOverallStatsChanges() {
		StatisticsData tempValue = this.overallStats;
		this.overallStats = this.draftOverallStats;
		tempValue.update(this.overallStats);
		this.draftOverallStats = tempValue;
	}
	
	
	
	private void removeFromPerodicStats(long removeTimeStamp) {
		StatisticsData perodicStats = perodicStatsMap.remove(removeTimeStamp);
		if(perodicStats!=null){
			draftOverallStats.setSum(draftOverallStats.getSum() - perodicStats.getSum());
			draftOverallStats.setCount(draftOverallStats.getCount() - perodicStats.getCount());
			draftOverallStats.setAvg(Helper.divide(draftOverallStats.getSum(), draftOverallStats.getCount()));
			// we need to find the max and min. value from all periodic statistics in case that the removed statistics was the min or max value.
			if (overallStats.getMin() == perodicStats.getMin() || overallStats.getMax() == perodicStats.getMax()) {
				double minValue = perodicStatsMap.isEmpty() ? 0d : Double.MAX_VALUE;
				double maxValue = 0;
				for (StatisticsData stats : perodicStatsMap.values()) {
					if (stats.getMin() < minValue) {
						minValue = stats.getMin();
					}
					if (stats.getMax() > maxValue) {
						maxValue = stats.getMax();
					}
				}
				draftOverallStats.setMin(minValue);
				draftOverallStats.setMax(maxValue);
			}
			commitOverallStatsChanges();
		}
	}

}
