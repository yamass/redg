package de.yamass.redg.plugin.util;

import org.junit.Assert;
import org.junit.Test;


public class ClassAvailabilityCheckerTest {

	@Test
	public void testHappyPaths() throws Exception {
		Assert.assertTrue(new ClassAvailabilityChecker("java.util.HashMap").isAvailable());
		Assert.assertFalse(new ClassAvailabilityChecker("no.java.util.HashMap").isAvailable());
	}
	
	@Test(expected = NullPointerException.class)
	public void testParameterContract() throws Exception {
		new ClassAvailabilityChecker(null);
	}

}
