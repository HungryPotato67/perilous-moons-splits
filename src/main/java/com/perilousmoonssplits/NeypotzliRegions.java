package com.perilousmoonssplits;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Region IDs for Neypotzli. Runs can start from the antechamber or a boss chamber.
 * Additional regions cover the wider dungeon for overlay / run tracking.
 */
final class NeypotzliRegions
{
	static final int ANTECHAMBER_REGION_ID = 6037;
	/**
	 * Note: world tiles around the Lunar Chest (e.g. 1513,9589) are also in region 6037.
	 * Do not treat "entered 6037 / antechamber run-start" as leaving the chest room.
	 */
	static final int ANCIENT_PRISON_REGION_ID = 5525;
	static final int EARTHBOUND_SOUTH_REGION_ID = 5782;
	static final int EARTHBOUND_NORTH_REGION_ID = 5783;

	/**
	 * Cam Torum city shares this region ID with the southern edge of Earthbound Cavern
	 * (NW door from the antechamber). Treat as dungeon only when another confirmed
	 * Neypotzli region is loaded in the scene.
	 */
	static final int CAM_TORUM_REGION_ID = 5781;

	// Eclipse Moon chamber only. Blue and Blood chambers share region IDs with
	// preparation caverns, so those are detected via boss NPC presence instead.
	private static final Set<Integer> BOSS_CHAMBER_REGION_IDS = new HashSet<>(Arrays.asList(
		6038
	));

	/** Regions that are always Neypotzli (never Cam Torum city alone). */
	private static final Set<Integer> NEYPOTZLI_REGION_IDS = new HashSet<>(Arrays.asList(
		// Ancient Prison / Blood Moon side
		5525, 5526, 5527,
		// Earthbound / Blue Moon prep corridors (NOT 5781 — ambiguous with Cam Torum)
		5782, 5783, 5784,
		// Antechamber / Eclipse / Streambound / shrine side
		6036, 6037, 6038, 6039, 6040, 6041, 6042, 6043, 6044
	));

	/**
	 * Prep campsite caverns where a new run should start after looting the chest.
	 * Excludes the Ancient Shrine / chest room and connecting corridors so walking
	 * from the Lunar Chest toward the next cavern does not reset mid-hallway.
	 */
	private static final Set<Integer> PREP_CAMPSITE_REGION_IDS = new HashSet<>(Arrays.asList(
		// Ancient Prison campsite
		5525, 5526, 5527,
		// Earthbound campsite
		5782, 5783, 5784,
		// Streambound campsite / fishing cavern (not shrine connectors)
		6039, 6040, 6042, 6043, 6044
	));

	/** Ancient Shrine / chest-side connectors — dungeon, but not a prep campsite. */
	private static final Set<Integer> SHRINE_REGION_IDS = new HashSet<>(Arrays.asList(
		6036, 6041
	));

	private NeypotzliRegions()
	{
	}

	static boolean isInAntechamber(Client client)
	{
		return isAntechamberRegion(getPlayerRegion(client));
	}

	static boolean isAntechamberRegion(int regionId)
	{
		return regionId == ANTECHAMBER_REGION_ID;
	}

	static boolean isInNeypotzli(Client client)
	{
		if (client == null || client.getLocalPlayer() == null)
		{
			return false;
		}

		int rawRegionId = getRawPlayerRegion(client);
		if (isNeypotzliRegion(rawRegionId))
		{
			return true;
		}

		int translatedRegionId = getPlayerRegion(client);
		if (isNeypotzliRegion(translatedRegionId))
		{
			return true;
		}

		// Earthbound's southern tiles share Cam Torum's region ID (5781).
		// Only count that region as dungeon when a confirmed Neypotzli region is loaded.
		if (rawRegionId == CAM_TORUM_REGION_ID || translatedRegionId == CAM_TORUM_REGION_ID)
		{
			return hasConfirmedDungeonRegionLoaded(client);
		}

		return false;
	}

	static boolean isNeypotzliRegion(int regionId)
	{
		return NEYPOTZLI_REGION_IDS.contains(regionId);
	}

	static boolean isInBossChamber(Client client)
	{
		return isBossChamberRegion(getPlayerRegion(client));
	}

	static boolean isBossChamberRegion(int regionId)
	{
		return BOSS_CHAMBER_REGION_IDS.contains(regionId);
	}

	static boolean isInRunStartArea(Client client)
	{
		return isRunStartRegion(getPlayerRegion(client));
	}

	static boolean isEffectiveRunStartArea(Client client, boolean bossPresent)
	{
		return isInRunStartArea(client) || (isInNeypotzli(client) && bossPresent);
	}

	static boolean isRunStartRegion(int regionId)
	{
		return isAntechamberRegion(regionId) || isBossChamberRegion(regionId);
	}

	static boolean isInPrepRoom(Client client, boolean bossPresent)
	{
		if (bossPresent || !isInNeypotzli(client))
		{
			return false;
		}

		return isPrepRoomRegion(getPlayerRegion(client), false)
			|| (getRawPlayerRegion(client) == CAM_TORUM_REGION_ID && hasConfirmedDungeonRegionLoaded(client));
	}

	static boolean isPrepRoomRegion(int regionId, boolean bossPresent)
	{
		if (!isNeypotzliRegion(regionId) || isRunStartRegion(regionId) || bossPresent)
		{
			return false;
		}

		return true;
	}

	/**
	 * True when standing in one of the three campsite caverns (Ancient Prison,
	 * Earthbound, Streambound), not the shrine/chest room or its connectors.
	 */
	static boolean isInPrepCampsite(Client client)
	{
		if (client == null || !isInNeypotzli(client))
		{
			return false;
		}

		int regionId = getPlayerRegion(client);
		if (isPrepCampsiteRegion(regionId))
		{
			return true;
		}

		// Earthbound's southern tiles share Cam Torum's region ID.
		return getRawPlayerRegion(client) == CAM_TORUM_REGION_ID
			&& hasConfirmedDungeonRegionLoaded(client);
	}

	static boolean isPrepCampsiteRegion(int regionId)
	{
		return PREP_CAMPSITE_REGION_IDS.contains(regionId);
	}

	static boolean isShrineRegion(int regionId)
	{
		return SHRINE_REGION_IDS.contains(regionId);
	}

	static boolean hasConfirmedDungeonRegionLoaded(Client client)
	{
		if (client == null)
		{
			return false;
		}

		int[] mapRegions = client.getMapRegions();
		if (mapRegions == null)
		{
			return false;
		}

		for (int mapRegion : mapRegions)
		{
			if (isNeypotzliRegion(mapRegion))
			{
				return true;
			}
		}

		return false;
	}

	static int getPlayerRegion(Client client)
	{
		WorldPoint location = getPlayerWorldPoint(client);
		return location == null ? -1 : location.getRegionID();
	}

	static WorldPoint getPlayerWorldPoint(Client client)
	{
		if (client == null || client.getLocalPlayer() == null)
		{
			return null;
		}

		WorldPoint raw = client.getLocalPlayer().getWorldLocation();
		if (raw != null && isNeypotzliRegion(raw.getRegionID()))
		{
			return raw;
		}

		LocalPoint localLocation = client.getLocalPlayer().getLocalLocation();
		if (localLocation == null)
		{
			return raw;
		}

		WorldPoint fromInstance = WorldPoint.fromLocalInstance(
			client,
			localLocation,
			raw == null ? 0 : raw.getPlane()
		);
		return fromInstance != null ? fromInstance : raw;
	}

	static int getRawPlayerRegion(Client client)
	{
		if (client == null || client.getLocalPlayer() == null)
		{
			return -1;
		}

		WorldPoint raw = client.getLocalPlayer().getWorldLocation();
		return raw == null ? -1 : raw.getRegionID();
	}
}
