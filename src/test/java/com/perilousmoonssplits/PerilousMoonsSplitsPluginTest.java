package com.perilousmoonssplits;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PerilousMoonsSplitsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PerilousMoonsSplitsPlugin.class);
		RuneLite.main(args);
	}
}
