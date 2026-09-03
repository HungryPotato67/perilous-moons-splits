package com.perilousmoonssplits;

import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.input.KeyListener;
import net.runelite.client.util.ColorUtil;

class ResetRunHotkeyListener implements KeyListener
{
	private final Client client;
	private final PerilousMoonsSplitsConfig config;
	private final RunTracker runTracker;

	@Inject
	ResetRunHotkeyListener(Client client, PerilousMoonsSplitsConfig config, RunTracker runTracker)
	{
		this.client = client;
		this.config = config;
		this.runTracker = runTracker;
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
			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				ColorUtil.wrapWithColorTag("Perilous Moons Splits run reset.", Color.ORANGE),
				null
			);
		}
	}

	@Override
	public void keyReleased(KeyEvent event)
	{
	}
}
