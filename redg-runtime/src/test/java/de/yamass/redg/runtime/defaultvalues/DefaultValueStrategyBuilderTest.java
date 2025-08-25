package de.yamass.redg.runtime.defaultvalues;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.runtime.defaultvalues.pluggable.IncrementingNumberProvider;
import de.yamass.redg.runtime.defaultvalues.pluggable.StaticNumberProvider;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultValueStrategyBuilderTest {
    @Test
    public void testWhen() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> false).thenUse(111);
        builder.when(columnModel -> true).thenUse(222);
        builder.when(columnModel -> false).thenUse(333);

        DefaultValueStrategy strategy = builder.build();

        Assert.assertEquals(222, strategy.getDefaultValue(Mockito.mock(ColumnModel.class), int.class).intValue());
    }

    @Test
    public void testWhenColumnNameMatches() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.whenColumnNameMatches(".*X").thenUse(999);

        DefaultValueStrategy strategy = builder.build();

        ColumnModel columnMock = Mockito.mock(ColumnModel.class);
        Mockito.when(columnMock.getDbName()).thenReturn("ASDFX");
        Assert.assertEquals(999, strategy.getDefaultValue(columnMock, int.class).intValue());
        Mockito.when(columnMock.getDbName()).thenReturn("ASDFA");
        Assert.assertNull("should return null since the default default value is null", strategy.getDefaultValue(columnMock, int.class));
    }

    @Test
    public void testWhenTableNameMatches() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.whenTableNameMatches(".*X").thenUse(999);

        DefaultValueStrategy strategy = builder.build();

        ColumnModel columnMock = Mockito.mock(ColumnModel.class);
        Mockito.when(columnMock.getDbTableName()).thenReturn("ASDFX");
        Assert.assertEquals(999, strategy.getDefaultValue(columnMock, int.class).intValue());
        Mockito.when(columnMock.getDbTableName()).thenReturn("ASDFA");
        Assert.assertNull(strategy.getDefaultValue(columnMock, int.class));
    }

    @Test
    public void testThenCompute() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> true).thenCompute((columnModel, aClass) -> 123);

        DefaultValueStrategy strategy = builder.build();

        Assert.assertEquals(123, strategy.getDefaultValue(Mockito.mock(ColumnModel.class), int.class).intValue());
    }

    @Test
    public void testThenUseProvider() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> true).thenUseProvider(new IncrementingNumberProvider());

        DefaultValueStrategy strategy = builder.build();

        ColumnModel columnModelMock = Mockito.mock(ColumnModel.class);
        Mockito.when(columnModelMock.getJavaTypeAsClass()).thenAnswer(invocationOnMock -> Integer.class);
        Assert.assertEquals(1, strategy.getDefaultValue(columnModelMock, int.class).intValue());
        Assert.assertEquals(2, strategy.getDefaultValue(columnModelMock, int.class).intValue());
        Assert.assertEquals(3, strategy.getDefaultValue(columnModelMock, int.class).intValue());
    }

    @Test
    public void testThenUseProvider2() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> columnModel.getDbName().startsWith("A")).thenUseProvider(new StaticNumberProvider(2));

        DefaultValueStrategy strategy = builder.build();

        ColumnModel columnModelMock = Mockito.mock(ColumnModel.class);
        Mockito.when(columnModelMock.getJavaTypeAsClass()).thenAnswer(invocationOnMock -> String.class);
        Assert.assertEquals(0, (int) strategy.getDefaultValue(TestUtils.getCM("", "", "A", String.class, true), int.class));
        Assert.assertEquals("-", strategy.getDefaultValue(TestUtils.getCM("", "", "B", String.class, true), String.class));
        Assert.assertEquals(2, (int) strategy.getDefaultValue(TestUtils.getCM("", "", "A", Integer.class, true), int.class));
    }

    @Test
    public void testSetFallbackStrategy() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> false).thenUse("asdf");
        builder.setFallbackStrategy(new DefaultValueStrategy() {
            @Override
            public <T> T getDefaultValue(ColumnModel columnModel, Class<T> type) {
                return (T) "fallback value";
            }
        });

        DefaultValueStrategy strategy = builder.build();

        Assert.assertEquals("fallback value", strategy.getDefaultValue(Mockito.mock(ColumnModel.class), String.class));
    }

    @Test
    public void testAndConditions() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.whenColumnNameMatches("a.*")
                .andColumnNameMatches(".*z")
                .andTableNameMatches("t.*")
                .and(ColumnModel::isPrimitiveType)
                .thenUse("matches!");

        DefaultValueStrategy strategy = builder.build();


        ColumnModel columnModel = prepareMock("able", "a__z", true);
        Assert.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));
        columnModel = prepareMock("table", "__z", true);
        Assert.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));
        columnModel = prepareMock("table", "a__", true);
        Assert.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));
        columnModel = prepareMock("table", "a__z", false);
        Assert.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));
        columnModel = prepareMock("table", "a__z", true);
        Assert.assertEquals("matches!", strategy.getDefaultValue(columnModel, String.class));
    }

    public ColumnModel prepareMock(String tableName, String columnName, boolean isPrimitiveType) {
        ColumnModel columnModel = Mockito.mock(ColumnModel.class);
        Mockito.when(columnModel.getDbTableName()).thenReturn(tableName);
        Mockito.when(columnModel.getDbName()).thenReturn(columnName);
        Mockito.when(columnModel.isPrimitiveType()).thenReturn(isPrimitiveType);
        return columnModel;
    }

}