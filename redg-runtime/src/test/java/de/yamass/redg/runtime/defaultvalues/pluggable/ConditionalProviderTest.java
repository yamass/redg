package de.yamass.redg.runtime.defaultvalues.pluggable;


import de.yamass.redg.models.ColumnModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionalProviderTest {

    @Test
    void testMatchAll() throws Exception {
        ConditionalProvider provider = new ConditionalProvider(new DefaultDefaultValueProvider(), ".*", ".*", ".*");

        assertThat(provider.willProvide(getModel("A", "B", "C.B"))).isTrue();
        assertThat(provider.willProvide(getModel("X", "Y", "Z.Y"))).isTrue();
    }

    @Test
    void testMatchPrefix() throws Exception {
        ConditionalProvider provider = new ConditionalProvider(new DefaultDefaultValueProvider(), ".*\\.REDG.*", "REDG.*", "REDG.*");

        assertThat(provider.willProvide(getModel("A", "B", "C.B"))).isFalse();
        assertThat(provider.willProvide(getModel("X", "Y", "Z.Y"))).isFalse();

        assertThat(provider.willProvide(getModel("REDG_A", "REDG_B", "C.REDG_B"))).isTrue();

        assertThat(provider.willProvide(getModel("A", "REDG_B", "C.REDG_B"))).isFalse();
        assertThat(provider.willProvide(getModel("REDG_A", "A", "C.REDG_B"))).isFalse();
        assertThat(provider.willProvide(getModel("REDG_A", "REDG_B", "C.B"))).isFalse();

        assertThat(provider.willProvide(getModel("REDG_A", "B", "C.B"))).isFalse();
        assertThat(provider.willProvide(getModel("A", "REDG_B", "C.B"))).isFalse();
        assertThat(provider.willProvide(getModel("A", "B", "C.REDG_B"))).isFalse();
    }

    @Test
    void testMatchFullTable() throws Exception {
        ConditionalProvider provider = new ConditionalProvider(new DefaultDefaultValueProvider(), ".*\\.REDG.*", null, null);

        assertThat(provider.willProvide(getModel("A", "B", "C.B"))).isFalse();
        assertThat(provider.willProvide(getModel("X", "Y", "Z.Y"))).isFalse();

        assertThat(provider.willProvide(getModel("REDG_A", "REDG_B", "C.REDG_B"))).isTrue();
        assertThat(provider.willProvide(getModel("A", "B", "C.REDG_B"))).isTrue();
        assertThat(provider.willProvide(getModel("C", "D", "F.REDG_A"))).isTrue();
    }

    @Test
    void testMatchTable() throws Exception {
        ConditionalProvider provider = new ConditionalProvider(new DefaultDefaultValueProvider(), null, "REDG.*", null);

        assertThat(provider.willProvide(getModel("A", "B", "C.B"))).isFalse();
        assertThat(provider.willProvide(getModel("X", "Y", "Z.Y"))).isFalse();

        assertThat(provider.willProvide(getModel("REDG_A", "REDG_B", "C.REDG_B"))).isTrue();
        assertThat(provider.willProvide(getModel("A", "REDG_B", "C.B"))).isTrue();
        assertThat(provider.willProvide(getModel("C", "REDG_D", "F.A"))).isTrue();
    }

    @Test
    void testMatchColumn() throws Exception {
        ConditionalProvider provider = new ConditionalProvider(new DefaultDefaultValueProvider(), null, null, "REDG.*");

        assertThat(provider.willProvide(getModel("A", "B", "C.B"))).isFalse();
        assertThat(provider.willProvide(getModel("X", "Y", "Z.Y"))).isFalse();

        assertThat(provider.willProvide(getModel("REDG_A", "REDG_B", "C.REDG_B"))).isTrue();
        assertThat(provider.willProvide(getModel("REDG_A", "B", "C.B"))).isTrue();
        assertThat(provider.willProvide(getModel("REDG_C", "D", "F.A"))).isTrue();
    }

    @Test
    void testGetDefaultValue() {
        ConditionalProvider provider = new ConditionalProvider(new NoProvider(), null, null, null);

        assertThat(provider.getDefaultValue(getModel("A", "B", "C.B"), String.class)).isNull();
    }

    private ColumnModel getModel(final String column, final String table, final String fullTable) {
        final ColumnModel model = new ColumnModel();
        model.setDbName(column);
        model.setDbTableName(table);
        model.setDbFullTableName(fullTable);
        return model;
    }

    public static class NoProvider implements PluggableDefaultValueProvider {

        @Override
        public boolean willProvide(ColumnModel columnModel) {
            return false;
        }

        @Override
        public <T> T getDefaultValue(ColumnModel columnModel, Class<T> type) {
            return null;
        }
    }

}