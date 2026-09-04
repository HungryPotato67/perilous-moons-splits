package com.perilousmoonssplits;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class PerilousMoonsSplitsOverlay extends OverlayPanel
{
	private final Client client;
	private final PerilousMoonsSplitsConfig config;
	private final RunTracker runTracker;
	private final PersonalBestStore personalBestStore;

	@Inject
	private PerilousMoonsSplitsOverlay(
		Client client,
		PerilousMoonsSplitsConfig config,
		RunTracker runTracker,
		PersonalBestStore personalBestStore,
		PerilousMoonsSplitsPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.runTracker = runTracker;
		this.personalBestStore = personalBestStore;
		setPosition(OverlayPosition.TOP_LEFT);
		addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Toggle", "Route PBs", e -> plugin.toggleRoutePersonalBests());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay() || !runTracker.shouldShowOverlay(client))
		{
			return null;
		}

		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(config.overlayWidth(), 0));

		String orderSummary = runTracker.getOrderSummary();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(orderSummary.isEmpty()
				? "Perilous Moons Splits"
				: "Perilous Moons Splits [" + orderSummary + "]")
			.color(Color.ORANGE)
			.build());

		long nowMs = runTracker.getTimerNowMs();
		int activeSplitIndex = runTracker.getActiveSplitIndex();
		boolean hasStartedSplit = false;

		for (int i = 0; i < runTracker.getSplits().length; i++)
		{
			SplitData split = runTracker.getSplits()[i];
			boolean isActive = i == activeSplitIndex;
			hasStartedSplit |= split.isActive() || split.isComplete();

			String timeText = split.isComplete()
				? SplitData.formatDuration(split.getElapsedMs(split.getEndTimeMs()))
				: split.isActive()
					? SplitData.formatDuration(split.getElapsedMs(nowMs))
					: "--:--";

			Long personalBest = runTracker.getPersonalBestForSplit(i, personalBestStore);
			String pbText = personalBest == null ? "--:--" : SplitData.formatDuration(personalBest);
			Color lineColor = isActive ? Color.GREEN : Color.WHITE;

			panelComponent.getChildren().add(LineComponent.builder()
				.left(runTracker.getSplitLabel(i))
				.right(timeText + "  PB " + pbText)
				.leftColor(lineColor)
				.rightColor(lineColor)
				.build());

			if (config.showSupplies() && RunTracker.isBossSplit(i) && (split.isActive() || split.isComplete()))
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left("  Food / Potions")
					.right(split.getFoodUsed() + " / " + split.getPotionsUsed())
					.leftColor(Color.LIGHT_GRAY)
					.rightColor(Color.LIGHT_GRAY)
					.build());
			}
		}

		if (hasStartedSplit)
		{
			long totalMs = runTracker.getTotalElapsedMs(nowMs);
			Long totalPb = runTracker.getTotalPersonalBest(personalBestStore);
			String totalPbText = totalPb == null ? "--:--" : SplitData.formatDuration(totalPb);
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Total")
				.right(SplitData.formatDuration(totalMs) + "  PB " + totalPbText)
				.leftColor(Color.YELLOW)
				.rightColor(Color.YELLOW)
				.build());
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Route PBs")
			.right(config.showRoutePersonalBests() ? "Shown" : "Hidden")
			.leftColor(Color.CYAN)
			.rightColor(Color.CYAN)
			.build());

		return super.render(graphics);
	}
}
