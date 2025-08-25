package de.yamass.redg.runtime.defaultvalues.pluggable.buildermatchers;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.runtime.defaultvalues.TestUtils;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;


public class MatchersTest {

    @Test
    public void tableName() {
        final ColumnModel cm = TestUtils.getCM("", "table", "", String.class, true);
        final ColumnModel cm2 = TestUtils.getCM("", "wrong", "", String.class, true);
        assertThat(Matchers.tableName("table"::equals))
                .accepts(cm)
                .rejects(cm2);
    }

    @Test
    public void fullTableName() {
        final ColumnModel cm = TestUtils.getCM("table", "", "", String.class, true);
        final ColumnModel cm2 = TestUtils.getCM("wrong", "", "", String.class, true);
        assertThat(Matchers.fullTableName("table"::equals))
                .accepts(cm)
                .rejects(cm2);
    }

    @Test
    public void columnName() {
        final ColumnModel cm = TestUtils.getCM("", "", "column", String.class, true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "wrong", String.class, true);
        assertThat(Matchers.columnName("column"::equals))
                .accepts(cm)
                .rejects(cm2);
    }

    @Test
    public void type() {
        final ColumnModel cm = TestUtils.getCM("", "", "", String.class, true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "", Integer.class, true);
        assertThat(Matchers.type(String.class::equals))
                .accepts(cm)
                .rejects(cm2);
    }

    @Test
    public void notNull() {
        final ColumnModel cm = TestUtils.getCM("", "", "", String.class, true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "", String.class, false);
        assertThat(Matchers.notNull(Boolean::booleanValue))
                .accepts(cm)
                .rejects(cm2);
    }

    @Test
    public void isNotNull() {
        final ColumnModel cm = TestUtils.getCM("", "", "", String.class, true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "", String.class, false);
        assertThat(Matchers.isNotNull())
                .accepts(cm)
                .rejects(cm2);
    }

    @Test
    public void isUnique() {
        final ColumnModel cm = TestUtils.getCM("", "", "", String.class, true);
        cm.setUnique(true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "", String.class, false);
        cm2.setUnique(false);
        assertThat(Matchers.isUnique())
                .accepts(cm)
                .rejects(cm2);
    }

    @Test
    public void isPrimary() {
        final ColumnModel cm = TestUtils.getCM("", "", "", String.class, true);
        cm.setPartOfPrimaryKey(true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "", String.class, false);
        cm2.setPartOfPrimaryKey(false);
        assertThat(Matchers.isPrimary())
                .accepts(cm)
                .rejects(cm2);
    }

    @Test
    public void allOf() {
        final ColumnModel cm = TestUtils.getCM("", "", "", String.class, true);
        cm.setPartOfPrimaryKey(true);
        cm.setUnique(true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "", String.class, false);
        cm2.setPartOfPrimaryKey(false);
        cm2.setUnique(false);
        final ColumnModel cm3 = TestUtils.getCM("", "", "", String.class, false);
        cm3.setPartOfPrimaryKey(true);
        cm3.setUnique(false);
        final ColumnModel cm4 = TestUtils.getCM("", "", "", String.class, false);
        cm4.setPartOfPrimaryKey(false);
        cm4.setUnique(true);
        assertThat(Matchers.allOf(Matchers.isPrimary(), Matchers.isUnique()))
                .accepts(cm)
                .rejects(cm2, cm3, cm4);
    }

    @Test
    public void anyOf() {
        final ColumnModel cm = TestUtils.getCM("", "", "", String.class, true);
        cm.setPartOfPrimaryKey(true);
        cm.setUnique(true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "", String.class, false);
        cm2.setPartOfPrimaryKey(false);
        cm2.setUnique(false);
        final ColumnModel cm3 = TestUtils.getCM("", "", "", String.class, false);
        cm3.setPartOfPrimaryKey(true);
        cm3.setUnique(false);
        final ColumnModel cm4 = TestUtils.getCM("", "", "", String.class, false);
        cm4.setPartOfPrimaryKey(false);
        cm4.setUnique(true);
        assertThat(Matchers.anyOf(Matchers.isPrimary(), Matchers.isUnique()))
                .accepts(cm, cm3, cm4)
                .rejects(cm2);
    }

    @Test
    public void oneOf() {
        final ColumnModel cm = TestUtils.getCM("", "", "", String.class, true);
        cm.setPartOfPrimaryKey(true);
        cm.setUnique(true);
        final ColumnModel cm2 = TestUtils.getCM("", "", "", String.class, false);
        cm2.setPartOfPrimaryKey(false);
        cm2.setUnique(false);
        final ColumnModel cm3 = TestUtils.getCM("", "", "", String.class, false);
        cm3.setPartOfPrimaryKey(true);
        cm3.setUnique(false);
        final ColumnModel cm4 = TestUtils.getCM("", "", "", String.class, false);
        cm4.setPartOfPrimaryKey(false);
        cm4.setUnique(true);
        assertThat(Matchers.oneOf(Matchers.isPrimary(), Matchers.isUnique()))
                .accepts(cm3, cm4)
                .rejects(cm2, cm);
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor c = Matchers.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(c.getModifiers()));

        c.setAccessible(true);
        c.newInstance();
    }
}