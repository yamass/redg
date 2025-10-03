package de.yamass.redg.runtime.defaultvalues.pluggable.buildermatchers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;


class ConditionsTest {

    @Test
    void eq() {
        assertThat(Conditions.eq("A"))
                .accepts("A")
                .rejects("B", "C");
    }

    @Test
    void neq() {
        assertThat(Conditions.neq("A"))
                .rejects("A")
                .accepts("B", "C");
    }

    @Test
    void contains() {
        assertThat(Conditions.contains("A"))
                .accepts("A", "AB", "BA", "ABBA")
                .rejects("B", "BODO");
    }

    @Test
    void matchesRegex() {
        assertThat(Conditions.matchesRegex("Hallo.+"))
                .accepts("Hallo Welt", "Hallo Joe")
                .rejects("Hello World");
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor c = Conditions.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(c.getModifiers()));

        c.setAccessible(true);
        c.newInstance();
    }
}