package com.perilousmoonssplits;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NeypotzliRegionsTest
{
	@Test
	public void camTorumCityIsNotNeypotzli()
	{
		// Cam Torum ~ (1440, 9568) => region 5781; leaving the antechamber returns here.
		assertFalse(NeypotzliRegions.isNeypotzliRegion(NeypotzliRegions.CAM_TORUM_REGION_ID));
		assertFalse(NeypotzliRegions.isPrepRoomRegion(NeypotzliRegions.CAM_TORUM_REGION_ID, false));
		assertFalse(NeypotzliRegions.isRunStartRegion(NeypotzliRegions.CAM_TORUM_REGION_ID));
	}

	@Test
	public void entranceStripOutsideDungeonIsNotNeypotzli()
	{
		assertFalse(NeypotzliRegions.isNeypotzliRegion(5780));
		assertFalse(NeypotzliRegions.isNeypotzliRegion(5781));
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
	public void shrineIsNotPrepCampsite()
	{
		assertTrue(NeypotzliRegions.isNeypotzliRegion(6036));
		assertTrue(NeypotzliRegions.isShrineRegion(6036));
		assertFalse(NeypotzliRegions.isPrepCampsiteRegion(6036));
		assertFalse(NeypotzliRegions.isPrepCampsiteRegion(6041));
	}

	@Test
	public void streamboundAndEarthboundArePrepCampsites()
	{
		assertTrue(NeypotzliRegions.isPrepCampsiteRegion(5782));
		assertTrue(NeypotzliRegions.isPrepCampsiteRegion(6039));
		assertTrue(NeypotzliRegions.isPrepCampsiteRegion(NeypotzliRegions.ANCIENT_PRISON_REGION_ID));
		assertFalse(NeypotzliRegions.isPrepCampsiteRegion(NeypotzliRegions.ANTECHAMBER_REGION_ID));
		assertFalse(NeypotzliRegions.isPrepCampsiteRegion(6038));
	}

	@Test
	public void earthboundCavernIsNeypotzli()
	{
		assertTrue(NeypotzliRegions.isNeypotzliRegion(5782));
		assertTrue(NeypotzliRegions.isNeypotzliRegion(5783));
		assertTrue(NeypotzliRegions.isNeypotzliRegion(5784));
		assertTrue(NeypotzliRegions.isPrepRoomRegion(5782, false));
		assertFalse(NeypotzliRegions.isNeypotzliRegion(NeypotzliRegions.CAM_TORUM_REGION_ID));
		assertFalse(NeypotzliRegions.isBossChamberRegion(5782));
		assertFalse(NeypotzliRegions.isRunStartRegion(5782));
		assertFalse(NeypotzliRegions.isRunStartRegion(5783));
	}

	@Test
	public void camTorumAloneIsNotPrepRoom()
	{
		assertFalse(NeypotzliRegions.isPrepRoomRegion(NeypotzliRegions.CAM_TORUM_REGION_ID, false));
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
