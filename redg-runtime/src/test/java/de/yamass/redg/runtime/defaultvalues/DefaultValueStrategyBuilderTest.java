package de.yamass.redg.runtime.defaultvalues;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.runtime.defaultvalues.pluggable.IncrementingNumberProvider;
import de.yamass.redg.runtime.defaultvalues.pluggable.StaticNumberProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultValueStrategyBuilderTest {
    @Test
    void testWhen() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> false).thenUse(111);
        builder.when(columnModel -> true).thenUse(222);
        builder.when(columnModel -> false).thenUse(333);

        DefaultValueStrategy strategy = builder.build();

        Assertions.assertEquals(222, strategy.getDefaultValue(Mockito.mock(ColumnModel.class), int.class).intValue());
    }

    @Test
    void testWhenColumnNameMatches() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.whenColumnNameMatches(".*X").thenUse(999);

        DefaultValueStrategy strategy = builder.build();

        ColumnModel columnMock = Mockito.mock(ColumnModel.class);
        Mockito.when(columnMock.getDbName()).thenReturn("ASDFX");
        Assertions.assertEquals(999, strategy.getDefaultValue(columnMock, int.class).intValue());
        Mockito.when(columnMock.getDbName()).thenReturn("ASDFA");
        Assertions.assertNull(strategy.getDefaultValue(columnMock, int.class), "should return null since the default default value is null");
    }

    @Test
    void testWhenTableNameMatches() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.whenTableNameMatches(".*X").thenUse(999);

        DefaultValueStrategy strategy = builder.build();

        ColumnModel columnMock = Mockito.mock(ColumnModel.class);
        Mockito.when(columnMock.getDbTableName()).thenReturn("ASDFX");
        Assertions.assertEquals(999, strategy.getDefaultValue(columnMock, int.class).intValue());
        Mockito.when(columnMock.getDbTableName()).thenReturn("ASDFA");
        Assertions.assertNull(strategy.getDefaultValue(columnMock, int.class));
    }

    @Test
    void testThenCompute() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> true).thenCompute((columnModel, aClass) -> 123);

        DefaultValueStrategy strategy = builder.build();

        Assertions.assertEquals(123, strategy.getDefaultValue(Mockito.mock(ColumnModel.class), int.class).intValue());
    }

    @Test
    void testThenUseProvider() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> true).thenUseProvider(new IncrementingNumberProvider());

        DefaultValueStrategy strategy = builder.build();

        ColumnModel columnModelMock = Mockito.mock(ColumnModel.class);
        Mockito.when(columnModelMock.getJavaTypeAsClass()).thenAnswer(invocationOnMock -> Integer.class);
        Assertions.assertEquals(1, strategy.getDefaultValue(columnModelMock, int.class).intValue());
        Assertions.assertEquals(2, strategy.getDefaultValue(columnModelMock, int.class).intValue());
        Assertions.assertEquals(3, strategy.getDefaultValue(columnModelMock, int.class).intValue());
    }

    @Test
    void testThenUseProvider2() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> columnModel.getDbName().startsWith("A")).thenUseProvider(new StaticNumberProvider(2));

        DefaultValueStrategy strategy = builder.build();

        ColumnModel columnModelMock = Mockito.mock(ColumnModel.class);
        Mockito.when(columnModelMock.getJavaTypeAsClass()).thenAnswer(invocationOnMock -> String.class);
        Assertions.assertEquals(0, (int) strategy.getDefaultValue(TestUtils.getCM("", "", "A", String.class, true), int.class));
        Assertions.assertEquals("-", strategy.getDefaultValue(TestUtils.getCM("", "", "B", String.class, true), String.class));
        Assertions.assertEquals(2, (int) strategy.getDefaultValue(TestUtils.getCM("", "", "A", Integer.class, true), int.class));
    }

    @Test
    void testSetFallbackStrategy() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.when(columnModel -> false).thenUse("asdf");
        builder.setFallbackStrategy(new DefaultValueStrategy() {
            @Override
            public <T> T getDefaultValue(ColumnModel columnModel, Class<T> type) {
                return (T) "fallback value";
            }
        });

        DefaultValueStrategy strategy = builder.build();

        Assertions.assertEquals("fallback value", strategy.getDefaultValue(Mockito.mock(ColumnModel.class), String.class));
    }

    @Test
    void testAndConditions() throws Exception {
        DefaultValueStrategyBuilder builder = new DefaultValueStrategyBuilder();

        builder.whenColumnNameMatches("a.*")
                .andColumnNameMatches(".*z")
                .andTableNameMatches("t.*")
                .and(ColumnModel::isPrimitiveType)
                .thenUse("matches!");

        DefaultValueStrategy strategy = builder.build();


        ColumnModel columnModel = prepareMock("able", "a__z", true);
        Assertions.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));
        columnModel = prepareMock("table", "__z", true);
        Assertions.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));
        columnModel = prepareMock("table", "a__", true);
        Assertions.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));
        columnModel = prepareMock("table", "a__z", false);
        Assertions.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));
        columnModel = prepareMock("table", "a__z", true);
        Assertions.assertEquals("matches!", strategy.getDefaultValue(columnModel, String.class));
    }

    public ColumnModel prepareMock(String tableName, String columnName, boolean isPrimitiveType) {
        ColumnModel columnModel = Mockito.mock(ColumnModel.class);
        Mockito.when(columnModel.getDbTableName()).thenReturn(tableName);
        Mockito.when(columnModel.getDbName()).thenReturn(columnName);
        Mockito.when(columnModel.isPrimitiveType()).thenReturn(isPrimitiveType);
        return columnModel;
    }

}