package com.n26.rohan.service;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class Helper {

	/**
	 * Round down the timestamp in seconds (drop the mills-seconds value)
	 * @param timestamp given timestamp
	 * @return rounded timestamp
	 */
	public static long roundDownToSeconds(long timestamp){
		return 1000 * (timestamp/ 1000);
	}
	
	/**
	 * Get the rounded elapsed time from the given timestamp until now. 
	 * @param roundedTimestamp given timeseconds with milliseconds rounded down.
	 * @return elapsed time in seconds
	 */
	public static long getElapsedDuration(long roundedTimestamp){
		return roundDownToSeconds(System.currentTimeMillis()) - roundedTimestamp;
	}

	/**
	 * Divide numbers using BigDecimal to avoid double precision problem
	 * @param value1 divided
	 * @param value2 divisor
	 * @return result of division as double (rounded down) 
	 */
	public static double divide(double value1, double value2){
		if (value2 == 0) {
			return 0;
		}
		BigDecimal value1Bd = BigDecimal.valueOf(value1);
		BigDecimal value2Bd = BigDecimal.valueOf(value2);
		return value1Bd.divide(value2Bd,RoundingMode.HALF_DOWN).doubleValue();
	}
	
	
	
	
}
