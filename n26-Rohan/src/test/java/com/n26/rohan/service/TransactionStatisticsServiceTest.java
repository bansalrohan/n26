package com.n26.rohan.service;

import static com.n26.rohan.service.TestUtils.sleep;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.n26.rohan.model.StatisticsData;
import com.n26.rohan.model.Transaction;
import com.n26.rohan.service.TransactionStatisticsService;


public class TransactionStatisticsServiceTest {

	private static final int PT_TRANS_MILLIS = 100;
	
	private TransactionStatisticsService service;

	@Before
    public void runBeforeTestMethod() {
		service = new TransactionStatisticsService();
    }
	
	@Test
	public void testAddTransaction() {
		double amount = 100.5;
		service.addTransaction(new Transaction(amount, System.currentTimeMillis()));
		sleep(PT_TRANS_MILLIS);
		assertEquals("Transaction didn't added", new StatisticsData(amount, amount, amount, amount, 1),
				service.getOverallStats());

	}

	@Test
	public void testAddingConncurrentTransaction() {
		Transaction[] transactions = { new Transaction(100, System.currentTimeMillis()), new Transaction(200, System.currentTimeMillis()),
				new Transaction(300, System.currentTimeMillis()+1000),new Transaction(-50, System.currentTimeMillis()+2000),
				new Transaction(250, System.currentTimeMillis()+5000),};
		addTransactionsConncurrently(transactions);
		while (service.hasTransactionsInQueue()) {

		}
		sleep(PT_TRANS_MILLIS);//processing of last element at the queue
		assertEquals("Stats calculation not work correctly with concurrent transactions", new StatisticsData(800, 160, 300, -50, 5),
				service.getOverallStats());
	}

	@Test
	public void testSlidingTransaction(){
		long timestamp = System.currentTimeMillis();
		Transaction[] transactions = { new Transaction(100, timestamp), new Transaction(200, timestamp),
				new Transaction(400, timestamp+30*1000),new Transaction(-50, timestamp+30*1000),
				new Transaction(250, System.currentTimeMillis()+50*1000),new Transaction(300, System.currentTimeMillis()+50*1000)};
		addTransactionsConncurrently(transactions);
		while (service.hasTransactionsInQueue()) {

		}
		sleep(PT_TRANS_MILLIS);//processing of last element at the queue
		assertEquals("All transaction stats", new StatisticsData(1200, 200, 400, -50, 6),
				service.getOverallStats());
		long sleep = 20*PT_TRANS_MILLIS +60*1000 - (System.currentTimeMillis() - timestamp); 
		sleep(sleep);
		assertEquals("After removing 1,2 transactions", new StatisticsData(900, 225, 400, -50, 4),
				service.getOverallStats());
		sleep(30*1000 + PT_TRANS_MILLIS);
		assertEquals("After removing 3,4 transactions", new StatisticsData(550, 275, 300, 250, 2),
				service.getOverallStats());
		sleep(20*1000 + PT_TRANS_MILLIS);
		
	}
	
	private ExecutorService addTransactionsConncurrently(final Transaction[] transactions){
		ExecutorService executor = Executors.newFixedThreadPool(3);
		for (final Transaction transaction : transactions) {
			Thread thread = new Thread(){
            	@Override
            	public void run() {
            		service.addTransaction(transaction);
            	}
            };
            executor.execute(thread);
		}
        return executor;
	}
	
	
		
}
