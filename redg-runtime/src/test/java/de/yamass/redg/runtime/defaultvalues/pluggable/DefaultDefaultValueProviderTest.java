package de.yamass.redg.runtime.defaultvalues.pluggable;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.runtime.defaultvalues.DefaultDefaultValueStrategyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DefaultDefaultValueProviderTest {


    @Test
    void willProvide() {
        assertThat(new DefaultDefaultValueProvider().willProvide(null)).isTrue();
        assertThat(new DefaultDefaultValueProvider().willProvide(mock(ColumnModel.class))).isTrue();
    }

    @Test
    void getDefaultValue() {
        DefaultDefaultValueProvider provider = new DefaultDefaultValueProvider();
        ColumnModel columnModel = new ColumnModel();

        columnModel.setNotNull(false);
        assertThat(provider.getDefaultValue(columnModel, String.class)).isNull();

        DefaultDefaultValueStrategyTest.defaultMappings.forEach((key, value) -> {
            final ColumnModel cm = new ColumnModel();
            cm.setNotNull(true);
            cm.setJavaTypeName(key.getName());
            Assertions.assertEquals(value, provider.getDefaultValue(cm, key));
        });
    }
}