package de.yamass.redg.runtime.defaultvalues.pluggable;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import de.yamass.redg.models.ColumnModel;

public class StaticNumberProviderTest {
    @Test
    public void getDefaultValue() throws Exception {
        StaticNumberProvider staticNumberProvider = new StaticNumberProvider(12L);

        Assert.assertEquals(12d, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), double.class), 0d);
        Assert.assertEquals(12f, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), float.class), 0f);
        Assert.assertEquals(12L, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), long.class).longValue());
        Assert.assertEquals(12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), int.class).intValue());
        Assert.assertEquals((byte) 12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), byte.class).byteValue());
        Assert.assertEquals((short) 12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), short.class).shortValue());
        Assert.assertEquals(new BigDecimal(12), staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), BigDecimal.class));
        Assert.assertEquals(12d, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Double.class), 0d);
        Assert.assertEquals(12f, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Float.class), 0f);
        Assert.assertEquals(12L, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Long.class).longValue());
        Assert.assertEquals(12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Integer.class).intValue());
        Assert.assertEquals((byte) 12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Byte.class).byteValue());
        Assert.assertEquals((short) 12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), Short.class).shortValue());
        Assert.assertEquals(12, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), AtomicInteger.class).get());
        Assert.assertEquals(12L, staticNumberProvider.getDefaultValue(Mockito.mock(ColumnModel.class), AtomicLong.class).get());

    }

}