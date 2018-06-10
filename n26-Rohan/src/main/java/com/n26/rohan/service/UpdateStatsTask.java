package com.n26.rohan.service;

import java.util.TimerTask;

/**
 * Scheduled job that run to check if we need to remove statistics from the current window
 * @author RohanBansal
 *
 */
public class UpdateStatsTask extends TimerTask{

	private StatisticsProcessor processor;
	
	public UpdateStatsTask(StatisticsProcessor processor){
		this.processor = processor;
	}
	
	@Override
	public void run() {
		processor.slidePerodicStatsicsWindow();
	}
		
}
