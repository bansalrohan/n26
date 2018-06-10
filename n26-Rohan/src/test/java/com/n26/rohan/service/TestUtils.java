package com.n26.rohan.service;

public class TestUtils {

	public static void sleep(long millis){
		try {
			Thread.sleep(millis); 
		} catch (InterruptedException e) {
		}
	}
}
