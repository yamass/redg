package de.yamass.redg.runtime.defaultvalues.pluggable;

import de.yamass.redg.models.ColumnModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class StaticNumberProviderTest {
    @Test
    void getDefaultValue() throws Exception {
        StaticNumberProvider staticNumberProvider = new StaticNumberProvider(12L);

        Assertions.assertEquals(12d, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), double.class), 0d);
        Assertions.assertEquals(12f, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), float.class), 0f);
        Assertions.assertEquals(12L, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), long.class).longValue());
        Assertions.assertEquals(12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), int.class).intValue());
        Assertions.assertEquals((byte) 12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), byte.class).byteValue());
        Assertions.assertEquals((short) 12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), short.class).shortValue());
        Assertions.assertEquals(new BigDecimal(12), staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), BigDecimal.class));
        Assertions.assertEquals(12d, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Double.class), 0d);
        Assertions.assertEquals(12f, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Float.class), 0f);
        Assertions.assertEquals(12L, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Long.class).longValue());
        Assertions.assertEquals(12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Integer.class).intValue());
        Assertions.assertEquals((byte) 12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Byte.class).byteValue());
        Assertions.assertEquals((short) 12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Short.class).shortValue());
        Assertions.assertEquals(12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), AtomicInteger.class).get());
        Assertions.assertEquals(12L, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), AtomicLong.class).get());

    }

}