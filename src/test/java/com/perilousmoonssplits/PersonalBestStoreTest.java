package com.perilousmoonssplits;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class PersonalBestStoreTest
{
	@Test
	public void splitKeysUseBossOrder()
	{
		assertEquals("prep1.BLOOD", PersonalBestStore.prep1Key(MoonsBoss.BLOOD));
		assertEquals("prep2.BLOOD>ECLIPSE", PersonalBestStore.prep2Key(MoonsBoss.BLOOD, MoonsBoss.ECLIPSE));
		assertEquals(
			"prep3.BLOOD>ECLIPSE>BLUE",
			PersonalBestStore.prep3Key(Arrays.asList(MoonsBoss.BLOOD, MoonsBoss.ECLIPSE, MoonsBoss.BLUE))
		);
		assertEquals("split1.BLOOD", PersonalBestStore.split1Key(MoonsBoss.BLOOD));
		assertEquals("split2.BLOOD>ECLIPSE", PersonalBestStore.split2Key(MoonsBoss.BLOOD, MoonsBoss.ECLIPSE));
		assertEquals(
			"split3.BLOOD>ECLIPSE>BLUE",
			PersonalBestStore.split3Key(Arrays.asList(MoonsBoss.BLOOD, MoonsBoss.ECLIPSE, MoonsBoss.BLUE))
		);
		assertEquals(
			"split3.BLUE>ECLIPSE>BLOOD",
			PersonalBestStore.split3Key(Arrays.asList(MoonsBoss.BLUE, MoonsBoss.ECLIPSE, MoonsBoss.BLOOD))
		);
		assertEquals(
			"total.BLOOD>ECLIPSE>BLUE",
			PersonalBestStore.totalKey(Arrays.asList(MoonsBoss.BLOOD, MoonsBoss.ECLIPSE, MoonsBoss.BLUE))
		);
	}
}
