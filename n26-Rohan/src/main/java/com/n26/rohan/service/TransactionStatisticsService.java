package com.n26.rohan.service;

import java.util.Timer;

import org.springframework.stereotype.Service;

import com.n26.rohan.model.StatisticsData;
import com.n26.rohan.model.Transaction;

@Service

public class TransactionStatisticsService {
	
	//we could also use spring injection
	private final StatisticsProcessor statsProcessor;
	
	private final TransactionProcessor transProcessor;
	
	
	public TransactionStatisticsService(){
		this.statsProcessor = new StatisticsProcessor();
		this.transProcessor = new TransactionProcessor(statsProcessor);
		Timer timer = new Timer();// 
		// we are going to set delay with zero because we may have transactions from past 
		timer.schedule(new UpdateStatsTask(statsProcessor), 0, 1000);
	}
	
	public void addTransaction(Transaction trans) {
		transProcessor.addTransaction(trans);
	}

	public StatisticsData getOverallStats() {
		return statsProcessor.getOverallStatistics();
	}

	protected boolean hasTransactionsInQueue() {
		return transProcessor.hasTransactionsInQueue();
	}
}
