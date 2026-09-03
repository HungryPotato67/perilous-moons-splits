package com.perilousmoonssplits;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;

/**
 * Controls whether dungeon overlays should render. Overlays are shown only while
 * the player is inside Neypotzli and are hidden after death until the player
 * leaves the dungeon and re-enters.
 */
@Singleton
class DungeonOverlayVisibility
{
	private final RunTracker runTracker;
	private boolean suppressedUntilReenter;

	@Inject
	DungeonOverlayVisibility(RunTracker runTracker)
	{
		this.runTracker = runTracker;
	}

	boolean shouldShowOverlay(Client client)
	{
		if (suppressedUntilReenter)
		{
			return false;
		}

		return isInDungeonActivity(client);
	}

	boolean shouldShowOverlayForRegion(int regionId)
	{
		return NeypotzliRegions.isNeypotzliRegion(regionId) && !suppressedUntilReenter;
	}

	void onPlayerDied()
	{
		suppressedUntilReenter = true;
	}

	void onLoggedIn(boolean fromLoginOrHop)
	{
		if (fromLoginOrHop)
		{
			suppressedUntilReenter = false;
		}
	}

	void onRegionChanged(Client client)
	{
		if (!isInDungeonActivity(client))
		{
			suppressedUntilReenter = false;
		}
	}

	void onLeftDungeonRegion(int regionId)
	{
		if (!NeypotzliRegions.isNeypotzliRegion(regionId))
		{
			suppressedUntilReenter = false;
		}
	}

	boolean isSuppressedUntilReenter()
	{
		return suppressedUntilReenter;
	}

	private boolean isInDungeonActivity(Client client)
	{
		if (client == null || client.getLocalPlayer() == null)
		{
			return false;
		}

		return NeypotzliRegions.isInNeypotzli(client)
			|| runTracker.isBossPresent()
			|| RunTracker.isBossInCombat(client);
	}
}
