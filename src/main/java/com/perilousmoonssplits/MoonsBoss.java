package com.perilousmoonssplits;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;

@Getter
public enum MoonsBoss
{
	BLOOD("Blood", NpcID.PMOON_BOSS_BLOOD_MOON_VIS, NpcID.PMOON_BOSS_BLOOD_MOON),
	ECLIPSE("Eclipse", NpcID.PMOON_BOSS_ECLIPSE_MOON_VIS, NpcID.PMOON_BOSS_ECLIPSE_MOON),
	BLUE("Blue", NpcID.PMOON_BOSS_BLUE_MOON_VIS, NpcID.PMOON_BOSS_BLUE_MOON);

	private static final List<List<MoonsBoss>> ALL_ROUTES = Collections.unmodifiableList(Arrays.asList(
		Arrays.asList(BLOOD, ECLIPSE, BLUE),
		Arrays.asList(BLOOD, BLUE, ECLIPSE),
		Arrays.asList(ECLIPSE, BLOOD, BLUE),
		Arrays.asList(ECLIPSE, BLUE, BLOOD),
		Arrays.asList(BLUE, BLOOD, ECLIPSE),
		Arrays.asList(BLUE, ECLIPSE, BLOOD)
	));

	private final String shortName;
	private final int[] npcIds;

	MoonsBoss(String shortName, int... npcIds)
	{
		this.shortName = shortName;
		this.npcIds = npcIds;
	}

	static List<List<MoonsBoss>> allRoutes()
	{
		return ALL_ROUTES;
	}

	static String formatRoute(List<MoonsBoss> route)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < route.size(); i++)
		{
			if (i > 0)
			{
				builder.append(" -> ");
			}
			builder.append(route.get(i).shortName);
		}
		return builder.toString();
	}

	static MoonsBoss fromNpcId(int npcId)
	{
		for (MoonsBoss boss : values())
		{
			for (int id : boss.npcIds)
			{
				if (id == npcId)
				{
					return boss;
				}
			}
		}
		return null;
	}

	static MoonsBoss fromDeathVarbit(int varbitId)
	{
		if (varbitId == VarbitID.PMOON_BOSS_BLOOD_DEAD)
		{
			return BLOOD;
		}
		if (varbitId == VarbitID.PMOON_BOSS_BLUE_DEAD)
		{
			return BLUE;
		}
		if (varbitId == VarbitID.PMOON_BOSS_ECLIPSE_DEAD)
		{
			return ECLIPSE;
		}
		return null;
	}
}
