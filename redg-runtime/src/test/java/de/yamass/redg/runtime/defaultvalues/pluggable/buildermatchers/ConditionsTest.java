package de.yamass.redg.runtime.defaultvalues.pluggable.buildermatchers;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;


public class ConditionsTest {

    @Test
    public void eq() {
        assertThat(Conditions.eq("A"))
                .accepts("A")
                .rejects("B", "C");
    }

    @Test
    public void neq() {
        assertThat(Conditions.neq("A"))
                .rejects("A")
                .accepts("B", "C");
    }

    @Test
    public void contains() {
        assertThat(Conditions.contains("A"))
                .accepts("A", "AB", "BA", "ABBA")
                .rejects("B", "BODO");
    }

    @Test
    public void matchesRegex() {
        assertThat(Conditions.matchesRegex("Hallo.+"))
                .accepts("Hallo Welt", "Hallo Joe")
                .rejects("Hello World");
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor c = Conditions.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(c.getModifiers()));

        c.setAccessible(true);
        c.newInstance();
    }
}