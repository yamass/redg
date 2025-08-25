package de.yamass.redg.runtime;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeMetaInfoTest {

    @Test
    public void test() {
        final AttributeMetaInfo info = new AttributeMetaInfo(
                "A",
                "B",
                "C",
                "D",
                42,
                String.class,
                true);
        assertThat(info.getDbColumnName()).isEqualTo("A");
        assertThat(info.getDbTableName()).isEqualTo("B");
        assertThat(info.getDbFullTableName()).isEqualTo("C");
        assertThat(info.getSqlType()).isEqualTo("D");
        assertThat(info.getSqlTypeInt()).isEqualTo(42);
        assertThat(info.getJavaType()).isEqualTo(String.class);
        assertThat(info.isNotNull()).isTrue();
    }

}