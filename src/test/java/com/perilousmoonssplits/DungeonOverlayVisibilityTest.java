package com.perilousmoonssplits;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DungeonOverlayVisibilityTest
{
	@Test
	public void overlayHiddenOutsideDungeon()
	{
		RunTracker tracker = new RunTracker();
		assertFalse(tracker.shouldShowOverlayForRegion(3200));
	}

	@Test
	public void overlayShownInsideDungeon()
	{
		RunTracker tracker = new RunTracker();
		assertTrue(tracker.shouldShowOverlayForRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));
	}

	@Test
	public void earthboundCavernIsNeypotzli()
	{
		assertTrue(NeypotzliRegions.isNeypotzliRegion(5782));
		assertTrue(NeypotzliRegions.isPrepRoomRegion(5782, false));
	}

	@Test
	public void playerDeathHidesOverlayUntilReenter()
	{
		RunTracker tracker = new RunTracker();
		assertTrue(tracker.shouldShowOverlayForRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));

		tracker.onPlayerDied();
		assertFalse(tracker.shouldShowOverlayForRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));
		assertFalse(tracker.shouldShowOverlayForRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID));
		assertFalse(tracker.shouldShowOverlayForRegion(5782));

		tracker.onLeftDungeonRegion(3200);
		assertFalse(tracker.shouldShowOverlayForRegion(3200));

		assertTrue(tracker.shouldShowOverlayForRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));
		assertTrue(tracker.shouldShowOverlayForRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID));
		assertTrue(tracker.shouldShowOverlayForRegion(5782));
	}

	@Test
	public void realLoginClearsDeathSuppression()
	{
		RunTracker tracker = new RunTracker();
		tracker.onPlayerDied();
		assertFalse(tracker.shouldShowOverlayForRegion(5782));

		tracker.onLoggedIn(false);
		assertFalse(tracker.shouldShowOverlayForRegion(5782));

		tracker.onLoggedIn(true);
		assertTrue(tracker.shouldShowOverlayForRegion(5782));
	}
}
