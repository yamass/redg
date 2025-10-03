package de.yamass.redg.plugin.util;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassAvailabilityCheckerTest {

	@Test
	public void testHappyPaths() throws Exception {
		assertTrue(new ClassAvailabilityChecker("java.util.HashMap").isAvailable());
		assertFalse(new ClassAvailabilityChecker("no.java.util.HashMap").isAvailable());
	}
	
	@Test
	public void testParameterContract() throws Exception {
		Assertions.assertThatThrownBy(() -> new ClassAvailabilityChecker(null))
				.isInstanceOf(NullPointerException.class);
	}

}
