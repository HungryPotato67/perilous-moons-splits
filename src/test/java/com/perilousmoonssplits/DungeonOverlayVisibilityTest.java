package com.perilousmoonssplits;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DungeonOverlayVisibilityTest
{
	private DungeonOverlayVisibility newVisibility()
	{
		return new DungeonOverlayVisibility(new RunTracker());
	}

	@Test
	public void overlayHiddenOutsideDungeon()
	{
		DungeonOverlayVisibility visibility = newVisibility();
		assertFalse(visibility.shouldShowOverlayForRegion(3200));
	}

	@Test
	public void overlayShownInsideDungeon()
	{
		DungeonOverlayVisibility visibility = newVisibility();
		assertTrue(visibility.shouldShowOverlayForRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));
	}

	@Test
	public void earthboundCavernIsNeypotzli()
	{
		// Blue Moon lobby / Earthbound campsite is around (1440, 9658) => region 5782
		assertTrue(NeypotzliRegions.isNeypotzliRegion(5782));
		assertTrue(NeypotzliRegions.isPrepRoomRegion(5782, false));
	}

	@Test
	public void playerDeathHidesOverlayUntilReenter()
	{
		DungeonOverlayVisibility visibility = newVisibility();
		assertTrue(visibility.shouldShowOverlayForRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));

		visibility.onPlayerDied();
		assertFalse(visibility.shouldShowOverlayForRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));
		assertFalse(visibility.shouldShowOverlayForRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID));
		assertFalse(visibility.shouldShowOverlayForRegion(5782));

		visibility.onLeftDungeonRegion(3200);
		assertFalse(visibility.shouldShowOverlayForRegion(3200));

		assertTrue(visibility.shouldShowOverlayForRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));
		assertTrue(visibility.shouldShowOverlayForRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID));
		assertTrue(visibility.shouldShowOverlayForRegion(5782));
	}

	@Test
	public void realLoginClearsDeathSuppression()
	{
		DungeonOverlayVisibility visibility = newVisibility();
		visibility.onPlayerDied();
		assertFalse(visibility.shouldShowOverlayForRegion(5782));

		visibility.onLoggedIn(false);
		assertFalse(visibility.shouldShowOverlayForRegion(5782));

		visibility.onLoggedIn(true);
		assertTrue(visibility.shouldShowOverlayForRegion(5782));
	}
}
