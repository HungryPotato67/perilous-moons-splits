package com.perilousmoonssplits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SplitFormatterTest
{
	@Test
	public void formatsMinutesSecondsTenths()
	{
		assertEquals("0:00.0", SplitData.formatDuration(0));
		assertEquals("1:02.3", SplitData.formatDuration(62_350));
		assertEquals("--:--", SplitData.formatDuration(-1));
	}
}
