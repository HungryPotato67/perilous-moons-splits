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
	public void containsAllSixBossOrders()
	{
		List<List<MoonsBoss>> permutations = RoutePermutations.all();
		assertEquals(6, permutations.size());

		Set<String> routes = new HashSet<>();
		for (List<MoonsBoss> route : permutations)
		{
			assertEquals(3, route.size());
			routes.add(RoutePermutations.formatRoute(route));
		}

		assertTrue(routes.contains("Blood -> Eclipse -> Blue"));
		assertTrue(routes.contains("Blood -> Blue -> Eclipse"));
		assertTrue(routes.contains("Eclipse -> Blood -> Blue"));
		assertTrue(routes.contains("Eclipse -> Blue -> Blood"));
		assertTrue(routes.contains("Blue -> Blood -> Eclipse"));
		assertTrue(routes.contains("Blue -> Eclipse -> Blood"));
	}

	@Test
	public void formatRouteUsesShortNames()
	{
		assertEquals(
			"Blood -> Eclipse -> Blue",
			RoutePermutations.formatRoute(Arrays.asList(MoonsBoss.BLOOD, MoonsBoss.ECLIPSE, MoonsBoss.BLUE))
		);
	}
}
