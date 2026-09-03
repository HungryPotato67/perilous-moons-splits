package com.perilousmoonssplits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class RoutePermutations
{
	private static final List<List<MoonsBoss>> ALL_PERMUTATIONS = buildPermutations();

	private RoutePermutations()
	{
	}

	static List<List<MoonsBoss>> all()
	{
		return ALL_PERMUTATIONS;
	}

	static String formatRoute(List<MoonsBoss> route)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < route.size(); i++)
		{
			if (i > 0)
			{
				builder.append(" -> ");
			}
			builder.append(route.get(i).getShortName());
		}
		return builder.toString();
	}

	private static List<List<MoonsBoss>> buildPermutations()
	{
		List<MoonsBoss> bosses = Arrays.asList(MoonsBoss.values());
		List<List<MoonsBoss>> permutations = new ArrayList<>();
		buildPermutations(bosses, 0, new ArrayList<>(), permutations);
		return Collections.unmodifiableList(permutations);
	}

	private static void buildPermutations(
		List<MoonsBoss> bosses,
		int index,
		List<MoonsBoss> current,
		List<List<MoonsBoss>> permutations)
	{
		if (index == bosses.size())
		{
			permutations.add(Collections.unmodifiableList(new ArrayList<>(current)));
			return;
		}

		for (MoonsBoss boss : bosses)
		{
			if (current.contains(boss))
			{
				continue;
			}

			current.add(boss);
			buildPermutations(bosses, index + 1, current, permutations);
			current.remove(current.size() - 1);
		}
	}
}
