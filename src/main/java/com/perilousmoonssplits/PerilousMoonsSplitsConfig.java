package com.perilousmoonssplits;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("perilous-moons-splits")
public interface PerilousMoonsSplitsConfig extends Config
{
	@ConfigItem(
		keyName = "showOverlay",
		name = "Show overlay",
		description = "Show the split timer overlay panel"
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSupplies",
		name = "Show supplies",
		description = "Show food and potion counts for each split"
	)
	default boolean showSupplies()
	{
		return true;
	}

	@ConfigItem(
		keyName = "overlayWidth",
		name = "Overlay width",
		description = "Width of the overlay panel in pixels"
	)
	default int overlayWidth()
	{
		return 400;
	}

	@ConfigItem(
		keyName = "showRoutePersonalBests",
		name = "Show route PBs",
		description = "Show a second overlay listing total personal bests for every boss order"
	)
	default boolean showRoutePersonalBests()
	{
		return false;
	}

	@ConfigItem(
		keyName = "routePbKeybind",
		name = "Toggle route PBs",
		description = "Toggle the route personal best overlay. You can also right-click the split overlay."
	)
	default Keybind routePbKeybind()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "permutationOverlayWidth",
		name = "Route PB overlay width",
		description = "Width of the route personal best overlay panel in pixels"
	)
	default int permutationOverlayWidth()
	{
		return 360;
	}

	@ConfigItem(
		keyName = "notifyPersonalBest",
		name = "Notify on PB",
		description = "Send a chat message when you beat a personal best split"
	)
	default boolean notifyPersonalBest()
	{
		return true;
	}

	@ConfigItem(
		keyName = "resetKeybind",
		name = "Reset run",
		description = "Reset the current run tracking"
	)
	default Keybind resetKeybind()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "resetPersonalBests",
		name = "Reset personal bests",
		description = "Reset all stored personal bests for this plugin",
		warning = "Are you sure you want to reset all personal bests?"
	)
	default boolean resetPersonalBests()
	{
		return false;
	}

	@ConfigItem(
		keyName = "debugMode",
		name = "Debug mode",
		description = "Log region and varbit changes to the developer console"
	)
	default boolean debugMode()
	{
		return false;
	}
}
