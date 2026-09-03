package com.perilousmoonssplits;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Region IDs for Neypotzli. Runs can start from the antechamber or a boss chamber.
 * Additional regions cover the wider dungeon for run reset detection.
 */
final class NeypotzliRegions
{
	static final int ANTECHAMBER_REGION_ID = 6037;
	static final int ANCIENT_PRISON_REGION_ID = 5525;

	// Eclipse Moon chamber only. Blue and Blood chambers share region IDs with
	// preparation caverns, so those are detected via boss NPC presence instead.
	private static final Set<Integer> BOSS_CHAMBER_REGION_IDS = new HashSet<>(Arrays.asList(
		6038
	));

	private static final Set<Integer> NEYPOTZLI_REGION_IDS = new HashSet<>(Arrays.asList(
		// Ancient Prison / Blood Moon side
		5524, 5525, 5526, 5527,
		// Earthbound / Blue Moon side
		5780, 5781, 5782, 5783, 5784,
		// Antechamber / Eclipse / Streambound / shrine side
		6035, 6036, 6037, 6038, 6039, 6040, 6041, 6042, 6043, 6044, 6045
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
		return isNeypotzliRegion(getPlayerRegion(client));
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
		return isPrepRoomRegion(getPlayerRegion(client), bossPresent);
	}

	static boolean isPrepRoomRegion(int regionId, boolean bossPresent)
	{
		if (!isNeypotzliRegion(regionId) || isRunStartRegion(regionId) || bossPresent)
		{
			return false;
		}

		return true;
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
		// Neypotzli caverns (including Earthbound) use normal overworld coordinates.
		// Prefer raw when it already maps to a known dungeon region so instance
		// translation after boss teleports cannot hide overlays / prep detection.
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
