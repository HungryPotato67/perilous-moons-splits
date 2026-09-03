package com.perilousmoonssplits;

import java.util.Arrays;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.gameval.NpcID;

@Getter
public enum MoonsBoss
{
	BLOOD("Blood", NpcID.PMOON_BOSS_BLOOD_MOON_VIS, NpcID.PMOON_BOSS_BLOOD_MOON),
	ECLIPSE("Eclipse", NpcID.PMOON_BOSS_ECLIPSE_MOON_VIS, NpcID.PMOON_BOSS_ECLIPSE_MOON),
	BLUE("Blue", NpcID.PMOON_BOSS_BLUE_MOON_VIS, NpcID.PMOON_BOSS_BLUE_MOON);

	private final String shortName;
	private final Set<Integer> npcIds;

	MoonsBoss(String shortName, int... npcIds)
	{
		this.shortName = shortName;
		this.npcIds = new java.util.HashSet<>();
		Arrays.stream(npcIds).forEach(this.npcIds::add);
	}

	public static MoonsBoss fromNpcId(int npcId)
	{
		for (MoonsBoss boss : values())
		{
			if (boss.npcIds.contains(npcId))
			{
				return boss;
			}
		}
		return null;
	}

	public static MoonsBoss fromDeathVarbit(int varbitId)
	{
		switch (varbitId)
		{
			case net.runelite.api.gameval.VarbitID.PMOON_BOSS_BLOOD_DEAD:
				return BLOOD;
			case net.runelite.api.gameval.VarbitID.PMOON_BOSS_BLUE_DEAD:
				return BLUE;
			case net.runelite.api.gameval.VarbitID.PMOON_BOSS_ECLIPSE_DEAD:
				return ECLIPSE;
			default:
				return null;
		}
	}

	public int getDeathVarbitId()
	{
		switch (this)
		{
			case BLOOD:
				return net.runelite.api.gameval.VarbitID.PMOON_BOSS_BLOOD_DEAD;
			case BLUE:
				return net.runelite.api.gameval.VarbitID.PMOON_BOSS_BLUE_DEAD;
			case ECLIPSE:
				return net.runelite.api.gameval.VarbitID.PMOON_BOSS_ECLIPSE_DEAD;
			default:
				throw new IllegalStateException("Unknown boss: " + this);
		}
	}

}
