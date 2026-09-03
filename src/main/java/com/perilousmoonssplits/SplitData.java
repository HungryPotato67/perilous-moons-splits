package com.perilousmoonssplits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SplitData
{
	private final int index;
	private long startTimeMs = -1;
	@Setter
	private long endTimeMs = -1;
	private int foodUsed;
	private int potionsUsed;

	SplitData(int index)
	{
		this.index = index;
	}

	void start(long nowMs)
	{
		startTimeMs = nowMs;
		endTimeMs = -1;
		foodUsed = 0;
		potionsUsed = 0;
	}

	void finish(long nowMs)
	{
		endTimeMs = nowMs;
	}

	void incrementFood()
	{
		foodUsed++;
	}

	void incrementPotions()
	{
		potionsUsed++;
	}

	long getElapsedMs(long nowMs)
	{
		if (startTimeMs < 0)
		{
			return -1;
		}

		long end = endTimeMs >= 0 ? endTimeMs : nowMs;
		return end - startTimeMs;
	}

	boolean isActive()
	{
		return startTimeMs >= 0 && endTimeMs < 0;
	}

	boolean isComplete()
	{
		return startTimeMs >= 0 && endTimeMs >= 0;
	}

	void reset()
	{
		startTimeMs = -1;
		endTimeMs = -1;
		foodUsed = 0;
		potionsUsed = 0;
	}
}
