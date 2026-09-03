package com.perilousmoonssplits;

import java.awt.event.KeyEvent;
import javax.inject.Inject;
import net.runelite.client.input.KeyListener;

class ToggleRoutePbHotkeyListener implements KeyListener
{
	private final PerilousMoonsSplitsConfig config;
	private final RoutePbToggle routePbToggle;

	@Inject
	ToggleRoutePbHotkeyListener(PerilousMoonsSplitsConfig config, RoutePbToggle routePbToggle)
	{
		this.config = config;
		this.routePbToggle = routePbToggle;
	}

	@Override
	public void keyTyped(KeyEvent event)
	{
	}

	@Override
	public void keyPressed(KeyEvent event)
	{
		if (config.routePbKeybind().matches(event))
		{
			routePbToggle.toggle();
		}
	}

	@Override
	public void keyReleased(KeyEvent event)
	{
	}
}
