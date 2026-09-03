package com.perilousmoonssplits;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

@Slf4j
@PluginDescriptor(name = "Perilous Moons Splits")
public class PerilousMoonsSplitsPlugin extends Plugin implements KeyListener
{
	private static final String CONFIG_GROUP = "perilous-moons-splits";

	@Inject private Client client;
	@Inject private PerilousMoonsSplitsConfig config;
	@Inject private OverlayManager overlayManager;
	@Inject private PerilousMoonsSplitsOverlay overlay;
	@Inject private PerilousMoonsPermutationOverlay permutationOverlay;
	@Inject private RunTracker runTracker;
	@Inject private PersonalBestStore personalBestStore;
	@Inject private KeyManager keyManager;
	@Inject private ConfigManager configManager;

	private int previousDeadBossCount = -1;
	private int previousRegionId = -1;
	private boolean previousInRunStartArea;
	private boolean previousInPrepRoom;
	private GameState previousGameState;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(permutationOverlay);
		keyManager.registerKeyListener(this);
		personalBestStore.load();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(permutationOverlay);
		keyManager.unregisterKeyListener(this);
		runTracker.manualReset();
	}

	@Override
	public void keyTyped(KeyEvent event)
	{
	}

	@Override
	public void keyPressed(KeyEvent event)
	{
		if (config.resetKeybind().matches(event))
		{
			runTracker.manualReset();
			chat("Perilous Moons Splits run reset.", Color.ORANGE);
		}
		else if (config.routePbKeybind().matches(event))
		{
			toggleRoutePersonalBests();
		}
	}

	@Override
	public void keyReleased(KeyEvent event)
	{
	}

	void toggleRoutePersonalBests()
	{
		boolean enabled = !config.showRoutePersonalBests();
		configManager.setConfiguration(CONFIG_GROUP, "showRoutePersonalBests", enabled);
		chat("Perilous Moons route PB overlay " + (enabled ? "shown" : "hidden") + ".", Color.ORANGE);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (MoonsBoss.fromNpcId(event.getNpc().getId()) != null)
		{
			runTracker.onBossNpcSpawned();
			runTracker.onBossPresenceChanged(client);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (MoonsBoss.fromNpcId(event.getNpc().getId()) != null)
		{
			runTracker.onBossNpcDespawned();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.LOGGED_IN)
		{
			boolean fromLoginOrHop = previousGameState == GameState.LOGIN_SCREEN
				|| previousGameState == GameState.LOGGING_IN
				|| previousGameState == GameState.HOPPING
				|| previousGameState == GameState.CONNECTION_LOST;

			previousDeadBossCount = -1;
			previousRegionId = -1;
			previousInRunStartArea = false;
			previousInPrepRoom = false;
			runTracker.onLoggedIn(fromLoginOrHop);
			if (config.debugMode())
			{
				log.debug("Logged in (fromLoginOrHop={}) - region tracking reset", fromLoginOrHop);
			}
		}
		previousGameState = gameState;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (config.debugMode())
		{
			logRegionState();
		}

		runTracker.onOverlayRegionChanged(client);
		runTracker.onRegionChanged(client);
		handleCompletedPrepSplit(runTracker.syncBossCombatState(client, System.currentTimeMillis()), "tick");

		int deadBossCount = RunTracker.getDeadBossCount(client);
		if (previousDeadBossCount < 0)
		{
			previousDeadBossCount = deadBossCount;
			return;
		}

		if (deadBossCount == 0 && previousDeadBossCount > 0)
		{
			if (config.debugMode())
			{
				log.debug("Boss death varbits reset - chest looted, waiting for next room");
			}
			runTracker.onChestLooted(client);
		}
		previousDeadBossCount = deadBossCount;
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		Actor actor = event.getActor();
		if (actor == client.getLocalPlayer())
		{
			runTracker.onPlayerDied();
			return;
		}
		if (!(actor instanceof NPC))
		{
			return;
		}

		MoonsBoss boss = MoonsBoss.fromNpcId(((NPC) actor).getId());
		if (boss != null)
		{
			handleBossKill(boss, "npc-death");
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varbitId = event.getVarbitId();
		if (varbitId == VarbitID.PMOON_BOSS_IN_COMBAT)
		{
			if (config.debugMode())
			{
				log.debug("PMOON_BOSS_IN_COMBAT={}", event.getValue());
			}
			handleCompletedPrepSplit(runTracker.syncBossCombatState(client, System.currentTimeMillis()), "varbit");
			return;
		}

		MoonsBoss boss = MoonsBoss.fromDeathVarbit(varbitId);
		if (boss != null && event.getValue() == 1)
		{
			handleBossKill(boss, "varbit");
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.PMOON_REWARD)
		{
			if (config.debugMode())
			{
				log.debug("Lunar chest reward interface opened - waiting for next room before starting next run");
			}
			runTracker.onChestLooted(client);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		String option = event.getMenuOption();
		if ("Eat".equals(option) || "Drink".equals(option))
		{
			runTracker.onEatOrDrink(option);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()) || !"resetPersonalBests".equals(event.getKey()))
		{
			return;
		}
		if (!config.resetPersonalBests())
		{
			return;
		}

		personalBestStore.clearAll();
		configManager.setConfiguration(CONFIG_GROUP, "resetPersonalBests", "false");
		chat("Perilous Moons personal bests reset.", Color.ORANGE);
	}

	@Provides
	PerilousMoonsSplitsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PerilousMoonsSplitsConfig.class);
	}

	private void handleBossKill(MoonsBoss boss, String source)
	{
		if (config.debugMode())
		{
			log.debug("Boss kill detected via {}: {}", source, boss);
		}

		int bossNumber = runTracker.getKillOrder().size();
		int completedSplitIndex = RunTracker.bossSplitIndex(bossNumber + 1);
		long durationMs = runTracker.onBossKilled(boss, System.currentTimeMillis());
		if (durationMs < 0)
		{
			return;
		}

		handlePersonalBest(completedSplitIndex, durationMs);
		if (runTracker.isRunComplete())
		{
			handleTotalPersonalBest();
		}
	}

	private void handleCompletedPrepSplit(int completedPrepIndex, String source)
	{
		if (completedPrepIndex < 0 || !RunTracker.isPrepSplit(completedPrepIndex))
		{
			return;
		}

		SplitData prepSplit = runTracker.getSplits()[completedPrepIndex];
		if (!prepSplit.isComplete())
		{
			return;
		}

		if (config.debugMode())
		{
			log.debug("Boss combat started via {} after {}", source, runTracker.getSplitLabel(completedPrepIndex));
		}
		handlePersonalBest(completedPrepIndex, prepSplit.getElapsedMs(prepSplit.getEndTimeMs()));
	}

	private void handlePersonalBest(int completedSplitIndex, long durationMs)
	{
		String pbKey = runTracker.getPersonalBestKey(completedSplitIndex);
		if (pbKey == null)
		{
			return;
		}

		if (personalBestStore.updatePersonalBest(pbKey, durationMs) && config.notifyPersonalBest())
		{
			chat(String.format(
				"Perilous Moons %s PB: %s",
				runTracker.getSplitLabel(completedSplitIndex),
				SplitData.formatDuration(durationMs)), Color.GREEN);
		}
	}

	private void handleTotalPersonalBest()
	{
		List<MoonsBoss> route = runTracker.getRouteOrder();
		if (route.size() < 3)
		{
			return;
		}

		long totalMs = runTracker.getTotalElapsedMs(System.currentTimeMillis());
		if (personalBestStore.updatePersonalBest(PersonalBestStore.totalKey(route), totalMs) && config.notifyPersonalBest())
		{
			chat("Perilous Moons Total PB: " + SplitData.formatDuration(totalMs), Color.GREEN);
		}
	}

	private void chat(String message, Color color)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag(message, color), null);
	}

	private void logRegionState()
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		int rawRegionId = NeypotzliRegions.getRawPlayerRegion(client);
		int regionId = NeypotzliRegions.getPlayerRegion(client);
		boolean inRunStartArea = NeypotzliRegions.isEffectiveRunStartArea(client, runTracker.isBossPresent());
		boolean inPrepRoom = NeypotzliRegions.isInPrepRoom(client, runTracker.isBossPresent());
		if (regionId == previousRegionId
			&& rawRegionId == previousRegionId
			&& inRunStartArea == previousInRunStartArea
			&& inPrepRoom == previousInPrepRoom)
		{
			return;
		}

		log.debug(
			"Region raw={} translated={} (neypotzli={}, antechamber={}, bossChamber={}, prepRoom={}, bossPresent={}, startArea={}, canStart={}, overlay={}, runActive={}, runComplete={}, awaitingNext={})",
			rawRegionId,
			regionId,
			NeypotzliRegions.isInNeypotzli(client),
			NeypotzliRegions.isInAntechamber(client),
			NeypotzliRegions.isInBossChamber(client),
			inPrepRoom,
			runTracker.isBossPresent(),
			inRunStartArea,
			RunTracker.canStartRunHere(inRunStartArea, inPrepRoom),
			runTracker.shouldShowOverlay(client),
			runTracker.isRunActive(),
			runTracker.isRunComplete(),
			runTracker.isAwaitingNextRun()
		);
		previousRegionId = rawRegionId != -1 ? rawRegionId : regionId;
		previousInRunStartArea = inRunStartArea;
		previousInPrepRoom = inPrepRoom;
	}
}
