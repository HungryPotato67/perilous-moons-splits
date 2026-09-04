package com.perilousmoonssplits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;

@Singleton
public class RunTracker
{
	static final int SPLIT_COUNT = 6;
	/** Minimum tiles moved from the chest before the next run can start in the same region. */
	static final int CHEST_LEAVE_DISTANCE = 10;

	@Getter
	private final List<MoonsBoss> killOrder = new ArrayList<>();

	/** Boss order from the previous completed run, used to show PBs before kills are confirmed. */
	private final List<MoonsBoss> assumedOrder = new ArrayList<>();

	@Getter
	private final SplitData[] splits = new SplitData[] {
		new SplitData(0),
		new SplitData(1),
		new SplitData(2),
		new SplitData(3),
		new SplitData(4),
		new SplitData(5)
	};

	private boolean runActive;
	private boolean runComplete;
	private boolean wasInNeypotzli;
	private boolean wasInRunStartArea;
	private boolean wasInPrepRoom;
	private boolean wasBossInCombat;
	private boolean awaitingNextRun;
	private int lootRegionId = -1;
	private WorldPoint lootLocation;
	private int visibleBossCount;
	private boolean overlaySuppressedUntilReenter;
	/** When set, active split clocks are frozen at this wall-clock time (logout / hop). */
	private long timerPausedAtMs = -1;

	public boolean isRunActive()
	{
		return runActive;
	}

	public boolean isRunComplete()
	{
		return runComplete;
	}

	public boolean hasRunResults()
	{
		return runActive || runComplete || !killOrder.isEmpty();
	}

	public int getActiveSplitIndex()
	{
		for (int i = 0; i < splits.length; i++)
		{
			if (splits[i].isActive())
			{
				return i;
			}
		}
		return -1;
	}

	static boolean isPrepSplit(int splitIndex)
	{
		return splitIndex >= 0 && splitIndex < SPLIT_COUNT && splitIndex % 2 == 0;
	}

	static boolean isBossSplit(int splitIndex)
	{
		return splitIndex >= 0 && splitIndex < SPLIT_COUNT && splitIndex % 2 == 1;
	}

	static int prepNumberFromSplit(int splitIndex)
	{
		return splitIndex / 2 + 1;
	}

	static int bossNumberFromSplit(int splitIndex)
	{
		return splitIndex / 2 + 1;
	}

	static int prepSplitIndex(int prepNumber)
	{
		return (prepNumber - 1) * 2;
	}

	static int bossSplitIndex(int bossNumber)
	{
		return bossNumber * 2 - 1;
	}

	public void onRegionChanged(Client client)
	{
		boolean inNeypotzli = NeypotzliRegions.isInNeypotzli(client);
		boolean inRunStartArea = NeypotzliRegions.isEffectiveRunStartArea(client, isBossPresent());
		boolean inPrepRoom = NeypotzliRegions.isInPrepRoom(client, isBossPresent());
		boolean canStartHere = canStartRunHere(inRunStartArea, inPrepRoom);
		int regionId = NeypotzliRegions.getPlayerRegion(client);

		if (awaitingNextRun && shouldStartAfterChestLoot(client, inNeypotzli, regionId))
		{
			startNextRun(client, System.currentTimeMillis());
		}
		else if (!awaitingNextRun)
		{
			if (!wasInNeypotzli && inNeypotzli)
			{
				tryStartRun(client, canStartHere);
			}

			if ((!wasInRunStartArea && inRunStartArea) || (!wasInPrepRoom && inPrepRoom))
			{
				tryStartRun(client, true);
			}
		}

		wasInNeypotzli = inNeypotzli;
		wasInRunStartArea = inRunStartArea;
		wasInPrepRoom = inPrepRoom;
	}

	public void onLoggedIn(boolean fromLoginOrHop)
	{
		wasInNeypotzli = false;
		wasInRunStartArea = false;
		wasInPrepRoom = false;
		wasBossInCombat = false;
		visibleBossCount = 0;

		// Always resume if paused. Login/hop often goes LOGGING_IN/HOPPING -> LOADING ->
		// LOGGED_IN, so previousGameState may be LOADING and fromLoginOrHop is false.
		resumeTimers(System.currentTimeMillis());

		if (fromLoginOrHop)
		{
			overlaySuppressedUntilReenter = false;
		}

		// Only clear completed-run display on a real login/hop. Moons teleports after a
		// boss kill can also emit LOGGED_IN via LOADING, and must keep the finished times.
		if (fromLoginOrHop && runComplete && !awaitingNextRun)
		{
			runActive = false;
			runComplete = false;
			killOrder.clear();
			for (SplitData split : splits)
			{
				split.reset();
			}
		}
	}

	void onLoggedOut()
	{
		pauseTimers(System.currentTimeMillis());
	}

	/**
	 * Wall-clock "now" for displaying / advancing active splits. Frozen while logged out.
	 */
	long getTimerNowMs()
	{
		return timerPausedAtMs >= 0 ? timerPausedAtMs : System.currentTimeMillis();
	}

	boolean isTimerPaused()
	{
		return timerPausedAtMs >= 0;
	}

	void pauseTimers(long nowMs)
	{
		if (timerPausedAtMs >= 0 || !runActive)
		{
			return;
		}
		timerPausedAtMs = nowMs;
	}

	void resumeTimers(long nowMs)
	{
		if (timerPausedAtMs < 0)
		{
			return;
		}

		long pausedForMs = Math.max(0, nowMs - timerPausedAtMs);
		if (pausedForMs > 0)
		{
			for (SplitData split : splits)
			{
				if (split.isActive())
				{
					split.shiftStart(pausedForMs);
				}
			}
		}
		timerPausedAtMs = -1;
	}

	boolean shouldShowOverlay(Client client)
	{
		if (overlaySuppressedUntilReenter)
		{
			return false;
		}
		if (client == null || client.getLocalPlayer() == null)
		{
			return false;
		}
		// Region only — boss combat varbits / NPC counts can stick after leaving the dungeon.
		return NeypotzliRegions.isInNeypotzli(client);
	}

	boolean shouldShowOverlayForRegion(int regionId)
	{
		return NeypotzliRegions.isNeypotzliRegion(regionId) && !overlaySuppressedUntilReenter;
	}

	void onPlayerDied()
	{
		overlaySuppressedUntilReenter = true;
	}

	void onOverlayRegionChanged(Client client)
	{
		if (!NeypotzliRegions.isInNeypotzli(client))
		{
			overlaySuppressedUntilReenter = false;
			visibleBossCount = 0;
		}
	}

	void onLeftDungeonRegion(int regionId)
	{
		if (!NeypotzliRegions.isNeypotzliRegion(regionId))
		{
			overlaySuppressedUntilReenter = false;
		}
	}

	public void onChestLooted(Client client)
	{
		// Only arm after a finished (or partially finished) run that is no longer active.
		if (runActive || killOrder.isEmpty())
		{
			return;
		}

		if (awaitingNextRun)
		{
			return;
		}

		awaitingNextRun = true;
		lootRegionId = NeypotzliRegions.getPlayerRegion(client);
		lootLocation = NeypotzliRegions.getPlayerWorldPoint(client);
		syncRegionState(client);
	}

	public void onBossNpcSpawned()
	{
		visibleBossCount++;
	}

	public void onBossPresenceChanged(Client client)
	{
		boolean inRunStartArea = NeypotzliRegions.isEffectiveRunStartArea(client, isBossPresent());
		boolean inPrepRoom = NeypotzliRegions.isInPrepRoom(client, isBossPresent());
		int regionId = NeypotzliRegions.getPlayerRegion(client);

		if (awaitingNextRun && shouldStartAfterChestLoot(
			client,
			NeypotzliRegions.isInNeypotzli(client),
			regionId))
		{
			startNextRun(client, System.currentTimeMillis());
		}
		else if (!awaitingNextRun
			&& ((!wasInRunStartArea && inRunStartArea) || (!wasInPrepRoom && inPrepRoom)))
		{
			tryStartRun(client, true);
		}
		wasInRunStartArea = inRunStartArea;
		wasInPrepRoom = inPrepRoom;
	}

	public void onBossNpcDespawned()
	{
		if (visibleBossCount > 0)
		{
			visibleBossCount--;
		}
	}

	boolean isBossPresent()
	{
		return visibleBossCount > 0;
	}

	/**
	 * @return completed prep split index, or -1 if no transition occurred
	 */
	public int syncBossCombatState(Client client, long nowMs)
	{
		if (!runActive || client == null)
		{
			return -1;
		}

		boolean inCombat = isBossInCombat(client);
		if (!wasBossInCombat && inCombat)
		{
			int completedPrepIndex = getActiveSplitIndex();
			if (onBossCombatStarted(nowMs) >= 0)
			{
				wasBossInCombat = inCombat;
				return completedPrepIndex;
			}
		}

		wasBossInCombat = inCombat;
		return -1;
	}

	/**
	 * @return duration of the completed prep split in milliseconds, or -1 if ignored
	 */
	public long onBossCombatStarted(long nowMs)
	{
		if (!runActive)
		{
			return -1;
		}

		int activeIndex = getActiveSplitIndex();
		if (!isPrepSplit(activeIndex) || !splits[activeIndex].isActive())
		{
			return -1;
		}

		SplitData prepSplit = splits[activeIndex];
		prepSplit.finish(nowMs);
		long durationMs = prepSplit.getElapsedMs(nowMs);
		startSplit(activeIndex + 1, nowMs);
		return durationMs;
	}

	/**
	 * @return duration of the completed boss split in milliseconds, or -1 if ignored
	 */
	public long onBossKilled(MoonsBoss boss, long nowMs)
	{
		if (!runActive || boss == null || killOrder.contains(boss))
		{
			return -1;
		}

		int bossNumber = killOrder.size() + 1;
		int splitIndex = bossSplitIndex(bossNumber);
		if (!assumedOrder.isEmpty() && killOrder.size() < assumedOrder.size()
			&& assumedOrder.get(killOrder.size()) != boss)
		{
			assumedOrder.clear();
		}
		if (splitIndex >= splits.length || getActiveSplitIndex() != splitIndex || !splits[splitIndex].isActive())
		{
			return -1;
		}

		SplitData split = splits[splitIndex];
		split.finish(nowMs);
		long durationMs = split.getElapsedMs(nowMs);
		killOrder.add(boss);

		if (killOrder.size() < 3)
		{
			startSplit(prepSplitIndex(killOrder.size() + 1), nowMs);
		}
		else
		{
			runActive = false;
			runComplete = true;
			assumedOrder.clear();
			assumedOrder.addAll(killOrder);
		}

		return durationMs;
	}

	public void onEatOrDrink(String action)
	{
		int activeIndex = getActiveSplitIndex();
		if (activeIndex < 0)
		{
			return;
		}

		if ("Eat".equals(action))
		{
			splits[activeIndex].incrementFood();
		}
		else if ("Drink".equals(action))
		{
			splits[activeIndex].incrementPotions();
		}
	}

	public void manualReset()
	{
		resetRun();
		wasInRunStartArea = false;
		wasInPrepRoom = false;
		awaitingNextRun = false;
		lootRegionId = -1;
		lootLocation = null;
	}

	/**
	 * Restarts only the currently active split timer and clears its food/potion counts.
	 * Completed splits and kill order are left unchanged.
	 *
	 * @return true if an active split was restarted
	 */
	public boolean restartCurrentSplit(long nowMs)
	{
		int activeIndex = getActiveSplitIndex();
		if (activeIndex < 0 || !runActive)
		{
			return false;
		}

		if (timerPausedAtMs >= 0)
		{
			timerPausedAtMs = nowMs;
		}

		splits[activeIndex].start(nowMs);
		return true;
	}

	void beginRun(long nowMs)
	{
		awaitingNextRun = false;
		lootRegionId = -1;
		lootLocation = null;
		timerPausedAtMs = -1;
		runActive = true;
		runComplete = false;
		wasBossInCombat = false;
		killOrder.clear();
		for (SplitData split : splits)
		{
			split.reset();
		}
		startSplit(0, nowMs);
	}

	public String getSplitLabel(int splitIndex)
	{
		MoonsBoss first = getBossAt(0);
		MoonsBoss second = getBossAt(1);
		MoonsBoss third = getBossAt(2);

		switch (splitIndex)
		{
			case 0:
				return formatPrepLabel(1);
			case 1:
				return first != null ? first.getShortName() : "Boss 1";
			case 2:
				return formatPrepLabel(2);
			case 3:
				return second != null ? second.getShortName() : "Boss 2";
			case 4:
				return formatPrepLabel(3);
			case 5:
				return third != null ? third.getShortName() : "Boss 3";
			default:
				return "";
		}
	}

	public String getOrderSummary()
	{
		List<MoonsBoss> displayOrder = getDisplayOrder();
		if (displayOrder.isEmpty())
		{
			return "";
		}

		String summary = formatOrderSummary(displayOrder);
		if (isAssumedOrderApplicable() && killOrder.size() < 3)
		{
			return summary + " (assumed)";
		}

		return summary;
	}

	public long getTotalElapsedMs(long nowMs)
	{
		long total = 0;
		for (SplitData split : splits)
		{
			if (split.isComplete())
			{
				total += split.getElapsedMs(split.getEndTimeMs());
			}
			else if (split.isActive())
			{
				total += split.getElapsedMs(nowMs);
			}
		}
		return total;
	}

	public Long getTotalPersonalBest(PersonalBestStore store)
	{
		List<MoonsBoss> route = getRouteOrder();
		if (route.size() < 3)
		{
			return null;
		}
		return store.getPersonalBest(PersonalBestStore.totalKey(route));
	}

	List<MoonsBoss> getRouteOrder()
	{
		List<MoonsBoss> displayOrder = getDisplayOrder();
		if (displayOrder.size() == 3)
		{
			return displayOrder;
		}
		return killOrder;
	}

	public Long getPersonalBestForSplit(int splitIndex, PersonalBestStore store)
	{
		String key = getPersonalBestKey(splitIndex);
		return key == null ? null : store.getPersonalBest(key);
	}

	String getPersonalBestKey(int splitIndex)
	{
		MoonsBoss first = getBossAt(0);
		MoonsBoss second = getBossAt(1);
		MoonsBoss third = getBossAt(2);

		if (isPrepSplit(splitIndex))
		{
			switch (prepNumberFromSplit(splitIndex))
			{
				case 1:
					return first == null ? null : PersonalBestStore.prep1Key(first);
				case 2:
					return first == null || second == null ? null : PersonalBestStore.prep2Key(first, second);
				case 3:
					return first == null || second == null || third == null
						? null
						: PersonalBestStore.prep3Key(Arrays.asList(first, second, third));
				default:
					return null;
			}
		}

		if (isBossSplit(splitIndex))
		{
			switch (bossNumberFromSplit(splitIndex))
			{
				case 1:
					return first == null ? null : PersonalBestStore.split1Key(first);
				case 2:
					return first == null || second == null ? null : PersonalBestStore.split2Key(first, second);
				case 3:
					return first == null || second == null || third == null
						? null
						: PersonalBestStore.split3Key(Arrays.asList(first, second, third));
				default:
					return null;
			}
		}

		return null;
	}

	static int getDeadBossCount(Client client)
	{
		int dead = 0;
		if (client.getVarbitValue(VarbitID.PMOON_BOSS_BLOOD_DEAD) == 1)
		{
			dead++;
		}
		if (client.getVarbitValue(VarbitID.PMOON_BOSS_BLUE_DEAD) == 1)
		{
			dead++;
		}
		if (client.getVarbitValue(VarbitID.PMOON_BOSS_ECLIPSE_DEAD) == 1)
		{
			dead++;
		}
		return dead;
	}

	static boolean isBossInCombat(Client client)
	{
		return client.getVarbitValue(VarbitID.PMOON_BOSS_IN_COMBAT) == 1;
	}

	private boolean shouldStartAfterChestLoot(
		Client client,
		boolean inNeypotzli,
		int regionId)
	{
		if (!inNeypotzli)
		{
			return false;
		}

		boolean inPrepCampsite = NeypotzliRegions.isInPrepCampsite(client);
		boolean leftChestRegion = regionId != -1 && lootRegionId != -1 && regionId != lootRegionId;

		return shouldStartNextRunAfterChest(
			inNeypotzli,
			inPrepCampsite,
			leftChestRegion,
			hasLeftChestArea(client)
		);
	}

	/**
	 * After looting, only start the next run once the player is in a prep campsite
	 * cavern (Ancient Prison / Earthbound / Streambound).
	 * <p>
	 * Region 6037 includes the Lunar Chest / shrine area, so antechamber / "run start"
	 * and distance-from-chest alone must not start the next run while still in that room.
	 */
	static boolean shouldStartNextRunAfterChest(
		boolean inNeypotzli,
		boolean inPrepCampsite,
		boolean leftChestRegion,
		boolean leftChestAreaByDistance)
	{
		if (!inNeypotzli || !inPrepCampsite)
		{
			return false;
		}

		// Prefer a real transition into a campsite region away from the chest.
		if (leftChestRegion)
		{
			return true;
		}

		// Same-region fallback only if the campsite somehow shares the chest region.
		return leftChestAreaByDistance;
	}

	private boolean hasLeftChestArea(Client client)
	{
		if (lootLocation == null || client == null)
		{
			return false;
		}

		WorldPoint current = NeypotzliRegions.getPlayerWorldPoint(client);
		return current != null && current.distanceTo(lootLocation) >= CHEST_LEAVE_DISTANCE;
	}

	void startNextRunAfterChestLoot(long nowMs)
	{
		if (awaitingNextRun)
		{
			startNextRun(null, nowMs);
		}
	}

	private void startNextRun(Client client, long nowMs)
	{
		beginRun(nowMs);
		if (client != null && isBossInCombat(client))
		{
			wasBossInCombat = true;
			onBossCombatStarted(nowMs);
		}
	}

	static boolean canStartRunHere(boolean inRunStartArea, boolean inPrepRoom)
	{
		return inRunStartArea || inPrepRoom;
	}

	boolean isAwaitingNextRun()
	{
		return awaitingNextRun;
	}

	private void tryStartRun(Client client, boolean canStartHere)
	{
		if (awaitingNextRun || runActive || runComplete || !canStartHere || getDeadBossCount(client) != 0)
		{
			return;
		}

		long nowMs = System.currentTimeMillis();
		beginRun(nowMs);
		if (isBossInCombat(client))
		{
			wasBossInCombat = true;
			onBossCombatStarted(nowMs);
		}
	}

	private void startSplit(int splitIndex, long nowMs)
	{
		splits[splitIndex].start(nowMs);
	}

	private void resetRun()
	{
		runActive = false;
		runComplete = false;
		wasBossInCombat = false;
		awaitingNextRun = false;
		timerPausedAtMs = -1;
		lootRegionId = -1;
		lootLocation = null;
		killOrder.clear();
		for (SplitData split : splits)
		{
			split.reset();
		}
	}

	private void syncRegionState(Client client)
	{
		if (client == null)
		{
			wasInNeypotzli = false;
			wasInRunStartArea = false;
			wasInPrepRoom = false;
			return;
		}

		wasInNeypotzli = NeypotzliRegions.isInNeypotzli(client);
		wasInRunStartArea = NeypotzliRegions.isEffectiveRunStartArea(client, isBossPresent());
		wasInPrepRoom = NeypotzliRegions.isInPrepRoom(client, isBossPresent());
	}

	MoonsBoss getBossAt(int index)
	{
		if (index < killOrder.size())
		{
			return killOrder.get(index);
		}

		if (isAssumedOrderApplicable() && index < assumedOrder.size())
		{
			return assumedOrder.get(index);
		}

		return null;
	}

	boolean isAssumedOrderApplicable()
	{
		if (assumedOrder.isEmpty())
		{
			return false;
		}

		if (killOrder.size() > assumedOrder.size())
		{
			return false;
		}

		for (int i = 0; i < killOrder.size(); i++)
		{
			if (killOrder.get(i) != assumedOrder.get(i))
			{
				return false;
			}
		}

		return true;
	}

	void seedAssumedOrderForTest(List<MoonsBoss> order)
	{
		assumedOrder.clear();
		assumedOrder.addAll(order);
	}

	private static String formatPrepLabel(int prepNumber)
	{
		return "Prep " + prepNumber;
	}

	private List<MoonsBoss> getDisplayOrder()
	{
		if (isAssumedOrderApplicable())
		{
			return new ArrayList<>(assumedOrder);
		}
		return new ArrayList<>(killOrder);
	}

	private static String formatOrderSummary(List<MoonsBoss> order)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < order.size(); i++)
		{
			if (i > 0)
			{
				builder.append(" -> ");
			}
			builder.append(order.get(i).getShortName());
		}
		return builder.toString();
	}
}
