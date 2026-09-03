package com.perilousmoonssplits;

import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.ColorUtil;

@Singleton
class RoutePbToggle
{
	private static final String CONFIG_GROUP = "perilous-moons-splits";
	private static final String CONFIG_KEY = "showRoutePersonalBests";

	private final Client client;
	private final ConfigManager configManager;
	private final PerilousMoonsSplitsConfig config;

	@Inject
	RoutePbToggle(Client client, ConfigManager configManager, PerilousMoonsSplitsConfig config)
	{
		this.client = client;
		this.configManager = configManager;
		this.config = config;
	}

	void toggle()
	{
		boolean enabled = !config.showRoutePersonalBests();
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, enabled);
		notifyToggle(enabled);
	}

	private void notifyToggle(boolean enabled)
	{
		String state = enabled ? "shown" : "hidden";
		client.addChatMessage(
			ChatMessageType.GAMEMESSAGE,
			"",
			ColorUtil.wrapWithColorTag("Perilous Moons route PB overlay " + state + ".", Color.ORANGE),
			null
		);
	}
}
