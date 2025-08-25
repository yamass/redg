package de.yamass.redg.runtime.defaultvalues.pluggable;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.runtime.defaultvalues.DefaultDefaultValueStrategyTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class DefaultDefaultValueProviderTest {


    @Test
    public void willProvide() {
        assertThat(new DefaultDefaultValueProvider().willProvide(null)).isTrue();
        assertThat(new DefaultDefaultValueProvider().willProvide(mock(ColumnModel.class))).isTrue();
    }

    @Test
    public void getDefaultValue() {
        DefaultDefaultValueProvider provider = new DefaultDefaultValueProvider();
        ColumnModel columnModel = new ColumnModel();

        columnModel.setNotNull(false);
        assertThat(provider.getDefaultValue(columnModel, String.class)).isNull();

        DefaultDefaultValueStrategyTest.defaultMappings.forEach((key, value) -> {
            final ColumnModel cm = new ColumnModel();
            cm.setNotNull(true);
            cm.setJavaTypeName(key.getName());
            assertEquals(value, provider.getDefaultValue(cm, key));
        });
    }
}