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
	private final DungeonOverlayVisibility dungeonOverlayVisibility;

	@Inject
	private PerilousMoonsPermutationOverlay(
		Client client,
		PerilousMoonsSplitsConfig config,
		PersonalBestStore personalBestStore,
		DungeonOverlayVisibility dungeonOverlayVisibility)
	{
		this.client = client;
		this.config = config;
		this.personalBestStore = personalBestStore;
		this.dungeonOverlayVisibility = dungeonOverlayVisibility;
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showRoutePersonalBests() || !dungeonOverlayVisibility.shouldShowOverlay(client))
		{
			return null;
		}

		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(config.permutationOverlayWidth(), 0));

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Route Total PBs")
			.color(Color.ORANGE)
			.build());

		for (List<MoonsBoss> route : RoutePermutations.all())
		{
			Long personalBest = personalBestStore.getPersonalBest(PersonalBestStore.totalKey(route));
			String pbText = personalBest == null ? "--:--" : SplitFormatter.formatDuration(personalBest);
			Color rightColor = personalBest == null ? Color.GRAY : Color.WHITE;

			panelComponent.getChildren().add(LineComponent.builder()
				.left(RoutePermutations.formatRoute(route))
				.right(pbText)
				.leftColor(Color.LIGHT_GRAY)
				.rightColor(rightColor)
				.build());
		}

		return super.render(graphics);
	}
}
