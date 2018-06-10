package com.n26.rohan.service;

import static com.n26.rohan.service.TestUtils.sleep;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.n26.rohan.model.Transaction;
import com.n26.rohan.service.StatisticsProcessor;
import com.n26.rohan.service.TransactionProcessor;

public class TransactionProcessorTest {

	@Test
	public void testAddTransaction(){
		StatisticsProcessor statsProcessorMock = mock(StatisticsProcessor.class);
		TransactionProcessor transactionProcessor = new TransactionProcessor(statsProcessorMock);
		
		Transaction transaction = new Transaction(1000, System.currentTimeMillis());
		transactionProcessor.addTransaction(transaction);
		sleep(500l);
		verify(statsProcessorMock, times(1)).processTransaction(transaction);
	}
	
	
	@Test
	public void testAddTransactionWithMultipleThreads(){
		StatisticsProcessor statsProcessorMock = mock(StatisticsProcessor.class);
		final TransactionProcessor transactionProcessor = new TransactionProcessor(statsProcessorMock);
		
		ExecutorService executor = Executors.newFixedThreadPool(5);
		for (int i = 0; i < 12; i++) {
			Thread thread = new Thread(){
            	@Override
            	public void run() {
            		transactionProcessor.addTransaction(new Transaction(100, 10l));
            	}
            };
            executor.execute(thread);
		}
		
		executor.shutdown();
		while(!executor.isTerminated() || transactionProcessor.hasTransactionsInQueue()){
			
		}
		sleep(500l);
		
		verify(statsProcessorMock, times(12)).processTransaction(any(Transaction.class));
	}
}
