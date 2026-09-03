package com.perilousmoonssplits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class RoutePermutationsTest
{
	@Test
	public void allBossOrdersAreUniqueAndComplete()
	{
		List<List<MoonsBoss>> permutations = MoonsBoss.allRoutes();
		assertEquals(6, permutations.size());

		Set<String> routes = new HashSet<>();
		for (List<MoonsBoss> route : permutations)
		{
			assertEquals(3, route.size());
			routes.add(MoonsBoss.formatRoute(route));
			assertEquals(3, new HashSet<>(route).size());
		}

		assertEquals(6, routes.size());
	}

	@Test
	public void formatRouteUsesShortNames()
	{
		assertEquals(
			"Blood -> Eclipse -> Blue",
			MoonsBoss.formatRoute(Arrays.asList(MoonsBoss.BLOOD, MoonsBoss.ECLIPSE, MoonsBoss.BLUE))
		);
	}
}
