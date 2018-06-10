package com.n26.rohan.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.n26.rohan.service.UpdateStatsTask;
import com.n26.rohan.service.StatisticsProcessor;

public class UpdateStatsTaskTest {

	@Test
	public void testAddTransaction() {

		StatisticsProcessor statsProcessorMock = mock(StatisticsProcessor.class);

		new UpdateStatsTask(statsProcessorMock).run();

		verify(statsProcessorMock, times(1)).slidePerodicStatsicsWindow();

	}
}
