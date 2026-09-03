package com.perilousmoonssplits;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
class PersonalBestStore
{
	private static final String CONFIG_GROUP = "perilous-moons-splits";
	private static final String PB_CONFIG_KEY = "personalBestsJson";

	private final ConfigManager configManager;
	private final Gson gson;
	private final Map<String, Long> personalBests = new HashMap<>();

	@Inject
	PersonalBestStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
		load();
	}

	void load()
	{
		personalBests.clear();
		String json = configManager.getConfiguration(CONFIG_GROUP, PB_CONFIG_KEY);
		if (json == null || json.isEmpty())
		{
			return;
		}

		JsonObject loaded = gson.fromJson(json, JsonObject.class);
		if (loaded == null)
		{
			return;
		}

		for (Map.Entry<String, JsonElement> entry : loaded.entrySet())
		{
			if (entry.getValue().isJsonPrimitive())
			{
				personalBests.put(entry.getKey(), entry.getValue().getAsLong());
			}
		}
	}

	Long getPersonalBest(String key)
	{
		return personalBests.get(key);
	}

	boolean updatePersonalBest(String key, long durationMs)
	{
		Long existing = personalBests.get(key);
		if (existing != null && durationMs >= existing)
		{
			return false;
		}

		personalBests.put(key, durationMs);
		save();
		return true;
	}

	void clearAll()
	{
		personalBests.clear();
		save();
	}

	private void save()
	{
		configManager.setConfiguration(CONFIG_GROUP, PB_CONFIG_KEY, gson.toJson(personalBests));
	}

	static String prep1Key(MoonsBoss firstBoss)
	{
		return "prep1." + firstBoss.name();
	}

	static String prep2Key(MoonsBoss firstBoss, MoonsBoss secondBoss)
	{
		return "prep2." + firstBoss.name() + ">" + secondBoss.name();
	}

	static String prep3Key(List<MoonsBoss> order)
	{
		return "prep3." + formatOrder(order);
	}

	static String split1Key(MoonsBoss firstBoss)
	{
		return "split1." + firstBoss.name();
	}

	static String split2Key(MoonsBoss firstBoss, MoonsBoss secondBoss)
	{
		return "split2." + firstBoss.name() + ">" + secondBoss.name();
	}

	static String split3Key(List<MoonsBoss> order)
	{
		return "split3." + formatOrder(order);
	}

	static String totalKey(List<MoonsBoss> order)
	{
		return "total." + formatOrder(order);
	}

	private static String formatOrder(List<MoonsBoss> order)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < order.size(); i++)
		{
			if (i > 0)
			{
				builder.append('>');
			}
			builder.append(order.get(i).name());
		}
		return builder.toString();
	}
}
