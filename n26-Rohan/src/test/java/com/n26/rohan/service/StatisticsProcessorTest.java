package com.n26.rohan.service;

import static com.n26.rohan.service.TestUtils.sleep;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.n26.rohan.model.StatisticsData;
import com.n26.rohan.model.Transaction;
import com.n26.rohan.service.StatisticsProcessor;

public class StatisticsProcessorTest {

	private StatisticsProcessor statsProcessor;

	@Before
	public void runBeforeTestMethod() {
		statsProcessor = new StatisticsProcessor();
	}

	@Test
	public void testAddTransaction() {
		double amount = 100.5;
		statsProcessor.processTransaction(new Transaction(amount, System.currentTimeMillis()));
		assertEquals("Transaction not processed correctly", new StatisticsData(amount, amount, amount, amount, 1),
				statsProcessor.getOverallStatistics());

	}

	@Test
	public void testAddTransactionFromPast() {
		statsProcessor.processTransaction(new Transaction(100, System.currentTimeMillis() - 70 * 1000));
		assertEquals("Past transations that older than overall period should not processed",
				statsProcessor.getOverallStatistics().getCount(), 0);

		statsProcessor.processTransaction(new Transaction(100, System.currentTimeMillis() - 50 * 1000));
		assertEquals("Transaction not processed correctly", statsProcessor.getOverallStatistics().getCount() , 1);

	}

	@Test
	public void testSlidePerodicStatsicsWindow() {
		long group1Ts = System.currentTimeMillis() - 40 * 1000;
		long group2Ts = group1Ts + 10 * 1000;
		long group3Ts = group2Ts + 10 * 1000;

		statsProcessor.processTransaction(new Transaction(100, group1Ts));
		statsProcessor.processTransaction(new Transaction(200, group1Ts));
		statsProcessor.processTransaction(new Transaction(400, group2Ts));
		statsProcessor.processTransaction(new Transaction(-50, group2Ts));
		statsProcessor.processTransaction(new Transaction(250, group3Ts));
		statsProcessor.processTransaction(new Transaction(300, group3Ts));

		assertEquals("All transaction stats", new StatisticsData(1200, 200, 400, -50, 6), statsProcessor.getOverallStatistics());
		
		statsProcessor.slidePerodicStatsicsWindow();
		assertEquals("Slid peroidc should not remove any item", 6, statsProcessor.getOverallStatistics().getCount());
		
		sleep(22*1000);
		statsProcessor.slidePerodicStatsicsWindow();
		assertEquals("Transactions 1,2 should be removed", 4,statsProcessor.getOverallStatistics().getCount());
		assertEquals("Statistics values after removing transactions 1,2 are not corrects", 
				new StatisticsData(900, 225, 400, -50, 4),statsProcessor.getOverallStatistics());
		
		sleep(12*1000);
		statsProcessor.slidePerodicStatsicsWindow();
		assertEquals("Transactions 3,4 should be removed", 2,statsProcessor.getOverallStatistics().getCount());
		assertEquals("Statistics values after removing transactions 3,4 are not corrects", 
				new StatisticsData(550, 275, 300, 250, 2),statsProcessor.getOverallStatistics());
		
		sleep(12*1000);
		statsProcessor.slidePerodicStatsicsWindow();
		assertEquals("Transactions 5,6 should be removed", 0,statsProcessor.getOverallStatistics().getCount());
		assertEquals("Statistics values after removing transactions 5,6 are not corrects", 
				new StatisticsData(0, 0, 0, 0, 0),statsProcessor.getOverallStatistics());
		
	}


}
