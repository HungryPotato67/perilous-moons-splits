package com.perilousmoonssplits;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SplitFormatterTest
{
	@Test
	public void formatsDuration()
	{
		assertEquals("0:00.0", SplitFormatter.formatDuration(0));
		assertEquals("1:02.3", SplitFormatter.formatDuration(62_350));
		assertEquals("--:--", SplitFormatter.formatDuration(-1));
	}
}
