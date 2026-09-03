package com.perilousmoonssplits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RunTrackerTest
{
	@Test
	public void bossKillAdvancesSplits()
	{
		RunTracker tracker = new RunTracker();
		tracker.beginRun(1_000);

		assertTrue(tracker.getSplits()[0].isActive());

		tracker.onBossCombatStarted(10_000);
		assertTrue(tracker.getSplits()[0].isComplete());
		assertTrue(tracker.getSplits()[1].isActive());

		long boss1 = tracker.onBossKilled(MoonsBoss.BLOOD, 61_000);
		assertEquals(51_000, boss1);
		assertTrue(tracker.getSplits()[1].isComplete());
		assertTrue(tracker.getSplits()[2].isActive());

		tracker.onBossCombatStarted(70_000);
		long boss2 = tracker.onBossKilled(MoonsBoss.ECLIPSE, 121_000);
		assertEquals(51_000, boss2);
		assertTrue(tracker.getSplits()[3].isComplete());
		assertTrue(tracker.getSplits()[4].isActive());

		tracker.onBossCombatStarted(130_000);
		long boss3 = tracker.onBossKilled(MoonsBoss.BLUE, 151_000);
		assertEquals(21_000, boss3);
		assertEquals("Blood -> Eclipse -> Blue", tracker.getOrderSummary());
		assertTrue(tracker.isRunComplete());
		assertFalse(tracker.isRunActive());
		assertTrue(tracker.hasRunResults());
	}

	@Test
	public void prepSplitEndsWhenBossCombatStarts()
	{
		RunTracker tracker = new RunTracker();
		tracker.beginRun(1_000);

		long prep1 = tracker.onBossCombatStarted(31_000);
		assertEquals(30_000, prep1);
		assertEquals("Prep 1", tracker.getSplitLabel(0));
		assertTrue(tracker.getSplits()[1].isActive());
	}

	@Test
	public void totalTimeSumsCompletedAndActiveSplits()
	{
		RunTracker tracker = new RunTracker();
		tracker.beginRun(0);
		tracker.onBossCombatStarted(10_000);
		tracker.onBossKilled(MoonsBoss.BLOOD, 30_000);

		assertEquals(40_000, tracker.getTotalElapsedMs(40_000));
	}

	@Test
	public void completedRunKeepsResultsAfterChestLoot()
	{
		RunTracker tracker = new RunTracker();
		tracker.beginRun(0);
		completeFullRun(tracker);

		assertTrue(tracker.isRunComplete());
		assertEquals(3, tracker.getKillOrder().size());
		assertTrue(tracker.getSplits()[5].isComplete());

		tracker.onChestLooted(null);

		assertTrue(tracker.isRunComplete());
		assertEquals(3, tracker.getKillOrder().size());
		assertTrue(tracker.getSplits()[5].isComplete());
	}

	@Test
	public void nextRoomAfterChestLootStartsNewRun()
	{
		RunTracker tracker = new RunTracker();
		completeFullRun(tracker);
		tracker.onChestLooted(null);

		tracker.startNextRunAfterChestLoot(10_000);

		assertTrue(tracker.isRunActive());
		assertFalse(tracker.isRunComplete());
		assertTrue(tracker.getKillOrder().isEmpty());
		assertTrue(tracker.getSplits()[0].isActive());
		assertFalse(tracker.getSplits()[5].isComplete());
	}

	@Test
	public void nextRunAfterChestLootKeepsAssumedOrder()
	{
		RunTracker tracker = new RunTracker();
		completeFullRun(tracker);

		tracker.onChestLooted(null);
		tracker.startNextRunAfterChestLoot(4_000);

		assertTrue(tracker.isAssumedOrderApplicable());
		assertEquals(MoonsBoss.BLOOD, tracker.getBossAt(0));
		assertEquals(MoonsBoss.ECLIPSE, tracker.getBossAt(1));
		assertEquals(MoonsBoss.BLUE, tracker.getBossAt(2));
		assertEquals("Blood -> Eclipse -> Blue (assumed)", tracker.getOrderSummary());
		assertEquals("Blood", tracker.getSplitLabel(1));
		assertEquals("Eclipse", tracker.getSplitLabel(3));
		assertEquals("Blue", tracker.getSplitLabel(5));
	}

	@Test
	public void chestLootWhileRunActiveIsIgnored()
	{
		RunTracker tracker = new RunTracker();
		completeFullRun(tracker);
		tracker.onChestLooted(null);
		tracker.startNextRunAfterChestLoot(4_000);

		assertTrue(tracker.isRunActive());
		tracker.onChestLooted(null);

		assertTrue(tracker.isRunActive());
		assertTrue(tracker.getSplits()[0].isActive());
	}

	@Test
	public void loginClearsCompletedRunSoPrepCanStart()
	{
		RunTracker tracker = new RunTracker();
		completeFullRun(tracker);
		assertTrue(tracker.isRunComplete());

		tracker.onLoggedIn(true);

		assertFalse(tracker.isRunComplete());
		assertFalse(tracker.isRunActive());
		assertTrue(tracker.isAssumedOrderApplicable());
		assertEquals("Blood -> Eclipse -> Blue (assumed)", tracker.getOrderSummary());
	}

	@Test
	public void bossTeleportLoginDoesNotClearCompletedRun()
	{
		RunTracker tracker = new RunTracker();
		completeFullRun(tracker);
		assertTrue(tracker.isRunComplete());
		assertTrue(tracker.getSplits()[5].isComplete());

		tracker.onLoggedIn(false);

		assertTrue(tracker.isRunComplete());
		assertEquals(3, tracker.getKillOrder().size());
		assertTrue(tracker.getSplits()[5].isComplete());
	}

	@Test
	public void prepRoomsCanStartRuns()
	{
		assertTrue(RunTracker.canStartRunHere(false, true));
		assertTrue(RunTracker.canStartRunHere(true, false));
		assertFalse(RunTracker.canStartRunHere(false, false));
	}

	@Test
	public void duplicateBossKillIsIgnored()
	{
		RunTracker tracker = new RunTracker();
		tracker.beginRun(0);
		tracker.onBossCombatStarted(1_000);
		assertEquals(2_000, tracker.onBossKilled(MoonsBoss.BLOOD, 3_000));
		assertEquals(-1, tracker.onBossKilled(MoonsBoss.BLOOD, 4_000));
	}

	@Test
	public void mismatchedKillClearsAssumedOrder()
	{
		RunTracker tracker = new RunTracker();
		completeFullRun(tracker);
		tracker.onChestLooted(null);
		tracker.startNextRunAfterChestLoot(4_000);

		tracker.onBossCombatStarted(5_000);
		tracker.onBossKilled(MoonsBoss.ECLIPSE, 6_000);

		assertFalse(tracker.isAssumedOrderApplicable());
		assertEquals(MoonsBoss.ECLIPSE, tracker.getBossAt(0));
		assertEquals(null, tracker.getBossAt(1));
		assertEquals("Prep 1", tracker.getSplitLabel(0));
		assertEquals("Eclipse", tracker.getSplitLabel(1));
		assertEquals("Prep 2", tracker.getSplitLabel(2));
		assertEquals("Eclipse", tracker.getOrderSummary());
	}

	private static void completeFullRun(RunTracker tracker)
	{
		tracker.beginRun(0);
		tracker.onBossCombatStarted(1_000);
		tracker.onBossKilled(MoonsBoss.BLOOD, 2_000);
		tracker.onBossCombatStarted(3_000);
		tracker.onBossKilled(MoonsBoss.ECLIPSE, 4_000);
		tracker.onBossCombatStarted(5_000);
		tracker.onBossKilled(MoonsBoss.BLUE, 6_000);
	}
}
