package de.yamass.redg.generator.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


class JavaSqlStringEscapeMapTest {

    @Test
    void get() {
        JavaSqlStringEscapeMap defaultMap = new JavaSqlStringEscapeMap();
        assertThat(defaultMap.get("asdf")).isEqualTo("\\\"asdf\\\"");
        assertThat(defaultMap.get("ASDF")).isEqualTo("\\\"ASDF\\\"");
        assertThat(defaultMap.get("\"TABLE\"")).isEqualTo("\\\"TABLE\\\"");

        JavaSqlStringEscapeMap mySqlMap = new JavaSqlStringEscapeMap("`");
        assertThat(mySqlMap.get("asdf")).isEqualTo("`asdf`");
        assertThat(mySqlMap.get("ASDF")).isEqualTo("`ASDF`");
        assertThat(mySqlMap.get("\"TABLE\"")).isEqualTo("`TABLE`");

        JavaSqlStringEscapeMap msSqlMap = new JavaSqlStringEscapeMap("[", "]");
        assertThat(msSqlMap.get("asdf")).isEqualTo("[asdf]");
        assertThat(msSqlMap.get("ASDF")).isEqualTo("[ASDF]");
        assertThat(msSqlMap.get("\"TABLE\"")).isEqualTo("[TABLE]");
    }

    @Test
    void containsKey() {
        JavaSqlStringEscapeMap map = new JavaSqlStringEscapeMap();
        assertThat(map.containsKey("A")).isTrue();
        assertThat(map.containsKey("Longer String")).isTrue();
        assertThat(map.containsKey(10)).isFalse();
        assertThat(map.containsKey(new BigDecimal(1))).isFalse();
        assertThat(map.containsKey(new Date())).isFalse();
    }
}