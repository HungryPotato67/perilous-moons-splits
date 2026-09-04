package com.perilousmoonssplits;

import lombok.Getter;
import lombok.Setter;

@Getter
class SplitData
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

	/** Move an active split's start forward so logout time is excluded from elapsed. */
	void shiftStart(long deltaMs)
	{
		if (startTimeMs >= 0 && endTimeMs < 0 && deltaMs > 0)
		{
			startTimeMs += deltaMs;
		}
	}

	static String formatDuration(long millis)
	{
		if (millis < 0)
		{
			return "--:--";
		}

		long totalCentiseconds = millis / 10;
		long centiseconds = totalCentiseconds % 100;
		long totalSeconds = totalCentiseconds / 100;
		long seconds = totalSeconds % 60;
		long minutes = totalSeconds / 60;
		return String.format("%d:%02d.%d", minutes, seconds, centiseconds / 10);
	}
}
