package com.perilousmoonssplits;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NeypotzliRegionsTest
{
	@Test
	public void entranceRoomIsNotRunStartArea()
	{
		assertTrue(NeypotzliRegions.isNeypotzliRegion(5781));
		assertFalse(NeypotzliRegions.isAntechamberRegion(5781));
		assertFalse(NeypotzliRegions.isBossChamberRegion(5781));
		assertFalse(NeypotzliRegions.isRunStartRegion(5781));
	}

	@Test
	public void antechamberIsRunStartArea()
	{
		assertTrue(NeypotzliRegions.isAntechamberRegion(6037));
		assertTrue(NeypotzliRegions.isRunStartRegion(6037));
	}

	@Test
	public void preparationCavernsAreNotRunStartAreas()
	{
		assertTrue(NeypotzliRegions.isNeypotzliRegion(5782));
		assertTrue(NeypotzliRegions.isNeypotzliRegion(5783));
		assertFalse(NeypotzliRegions.isBossChamberRegion(5782));
		assertFalse(NeypotzliRegions.isBossChamberRegion(5783));
		assertFalse(NeypotzliRegions.isRunStartRegion(5782));
		assertFalse(NeypotzliRegions.isRunStartRegion(5783));
		assertTrue(NeypotzliRegions.isPrepRoomRegion(5782, false));
		assertTrue(NeypotzliRegions.isPrepRoomRegion(5783, false));
	}

	@Test
	public void prepRoomsCanStartRuns()
	{
		assertTrue(RunTracker.canStartRunHere(false, true));
		assertTrue(RunTracker.canStartRunHere(true, false));
		assertFalse(RunTracker.canStartRunHere(false, false));
	}

	@Test
	public void ancientPrisonIsPrepRoom()
	{
		assertTrue(NeypotzliRegions.isPrepRoomRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID, false));
		assertFalse(NeypotzliRegions.isPrepRoomRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID, false));
		assertFalse(NeypotzliRegions.isPrepRoomRegion(6038, false));
		assertFalse(NeypotzliRegions.isPrepRoomRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID, true));
	}

	@Test
	public void ancientPrisonIsNeypotzliRegion()
	{
		assertTrue(NeypotzliRegions.isNeypotzliRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID));
		assertFalse(NeypotzliRegions.isRunStartRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID));
	}

	@Test
	public void eclipseChamberIsRunStartArea()
	{
		assertTrue(NeypotzliRegions.isBossChamberRegion(6038));
		assertTrue(NeypotzliRegions.isRunStartRegion(6038));
	}
}
