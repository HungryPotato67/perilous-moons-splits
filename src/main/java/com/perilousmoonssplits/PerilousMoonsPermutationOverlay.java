package com.perilousmoonssplits;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class PerilousMoonsPermutationOverlay extends OverlayPanel
{
	private final Client client;
	private final PerilousMoonsSplitsConfig config;
	private final PersonalBestStore personalBestStore;
	private final RunTracker runTracker;

	@Inject
	private PerilousMoonsPermutationOverlay(
		Client client,
		PerilousMoonsSplitsConfig config,
		PersonalBestStore personalBestStore,
		RunTracker runTracker)
	{
		this.client = client;
		this.config = config;
		this.personalBestStore = personalBestStore;
		this.runTracker = runTracker;
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showRoutePersonalBests() || !runTracker.shouldShowOverlay(client))
		{
			return null;
		}

		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(config.permutationOverlayWidth(), 0));
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Route Total PBs")
			.color(Color.ORANGE)
			.build());

		for (List<MoonsBoss> route : MoonsBoss.allRoutes())
		{
			Long personalBest = personalBestStore.getPersonalBest(PersonalBestStore.totalKey(route));
			panelComponent.getChildren().add(LineComponent.builder()
				.left(MoonsBoss.formatRoute(route))
				.right(personalBest == null ? "--:--" : SplitData.formatDuration(personalBest))
				.leftColor(Color.LIGHT_GRAY)
				.rightColor(personalBest == null ? Color.GRAY : Color.WHITE)
				.build());
		}

		return super.render(graphics);
	}
}
