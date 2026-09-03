package com.perilousmoonssplits;

final class SplitFormatter
{
	private SplitFormatter()
	{
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
