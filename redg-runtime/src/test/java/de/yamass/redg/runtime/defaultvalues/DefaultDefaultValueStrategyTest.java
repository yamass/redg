/*
 * Copyright Yann Massard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.yamass.redg.runtime.defaultvalues;

import de.yamass.redg.models.ColumnModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class DefaultDefaultValueStrategyTest {

    public static final Map<Class<?>, Object> defaultMappings = new HashMap<>();

    static {
        defaultMappings.put(String.class, "-");
        defaultMappings.put(Character.class, ' ');
        defaultMappings.put(Boolean.class, false);
        // Numbers
        defaultMappings.put(BigDecimal.class, new BigDecimal(0));
        defaultMappings.put(Double.class, 0.0);
        defaultMappings.put(Float.class, 0.0f);
        defaultMappings.put(Long.class, 0L);
        defaultMappings.put(Integer.class, 0);
        defaultMappings.put(Byte.class, (byte) 0);
        defaultMappings.put(Short.class, (short) 0);
        // SQL Date & Time
        defaultMappings.put(Date.class, new Date(0));
        defaultMappings.put(Time.class, new Time(0));
        defaultMappings.put(Timestamp.class, new Timestamp(0));
        // Java 8 Date & Time
        defaultMappings.put(LocalDate.class, LocalDate.of(1970, 1, 1));
        defaultMappings.put(LocalTime.class, LocalTime.of(0, 0, 0));
        defaultMappings.put(LocalDateTime.class, LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0));
        defaultMappings.put(ZonedDateTime.class, ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        defaultMappings.put(OffsetDateTime.class, OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        defaultMappings.put(OffsetTime.class, OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC));

    }

    @Test
    void testStrategy() {
        DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        ColumnModel columnModel = new ColumnModel();

        columnModel.setNotNull(false);
        Assertions.assertEquals(null, strategy.getDefaultValue(columnModel, String.class));

        defaultMappings.forEach((key, value) -> {
            final ColumnModel cm = new ColumnModel();
            cm.setNotNull(true);
            cm.setJavaTypeName(key.getName());
            Assertions.assertEquals(value, strategy.getDefaultValue(cm, key));
        });
    }

    @Test
    void testStrategy_Primitive() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setJavaTypeName("int");
        Assertions.assertEquals(0, new DefaultDefaultValueStrategy().getDefaultValue(cm, int.class).intValue());
    }

    @Test
    void testStrategy_NoValue() {
        DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        assertThatThrownBy(() -> strategy.getDefaultValue(cm, Color.class))
                .isInstanceOf(NoDefaultValueException.class)
                .hasMessageContaining("No default value for type");
    }

    @Test
    void testStrategy_UniqueString() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(Long.toString(i, 36), strategy.getDefaultValue(cm, String.class));
        }

    }

    @Test
    void testStrategy_UniqueNumber() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 200; i++) {
            Assertions.assertEquals(i, (int) strategy.getDefaultValue(cm, Integer.class));
        }
    }

    @Test
    void testStrategy_UniqueChar() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < Character.MAX_VALUE; i++) {
            Assertions.assertEquals((char) (i + 1), (char) strategy.getDefaultValue(cm, char.class));
        }
        assertThatThrownBy(() -> strategy.getDefaultValue(cm, Character.class))
                .isInstanceOf(NoDefaultValueException.class);

    }

    @Test
    void testStrategy_UniqueBoolean() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        Assertions.assertFalse(strategy.getDefaultValue(cm, boolean.class));
        Assertions.assertTrue(strategy.getDefaultValue(cm, Boolean.class));

        assertThatThrownBy(() -> strategy.getDefaultValue(cm, boolean.class))
                .isInstanceOf(NoDefaultValueException.class);

    }

    @Test
    void testStrategy_UniqueDate() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 200; i++) {
            Assertions.assertEquals(i, strategy.getDefaultValue(cm, Date.class).getTime());
        }
    }

    @Test
    void testStrategy_UniqueZonedDateTime() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 200; i++) {
            Assertions.assertEquals(ZonedDateTime.ofInstant(Instant.ofEpochMilli(i), ZoneId.systemDefault()), strategy.getDefaultValue(cm, ZonedDateTime.class));
        }
    }

    @Test
    void testStrategy_UniqueEnum() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < TestEnum.values().length; i++) {
            Assertions.assertEquals(TestEnum.values()[i], strategy.getDefaultValue(cm, TestEnum.class));
        }

        assertThatThrownBy(() -> strategy.getDefaultValue(cm, TestEnum.class))
                .isInstanceOf(NoDefaultValueException.class);
    }

    @Test
    void testStrategy_Enum() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(false);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(TestEnum.A, strategy.getDefaultValue(cm, TestEnum.class));
        }
    }

    @Test
    void testStrategy_NoUniquePossible() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        assertThatThrownBy(() -> strategy.getDefaultValue(cm, Object.class))
                .isInstanceOf(NoDefaultValueException.class);

    }

    @Test
    void testStrategy_EmptyUniqueEnum() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();

        assertThatThrownBy(() -> strategy.getDefaultValue(cm, EmptyEnum.class))
                .isInstanceOf(NoDefaultValueException.class);

    }

    @Test
    void testStrategy_EmptyEnum() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(false);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();

        assertThatThrownBy(() -> strategy.getDefaultValue(cm, EmptyEnum.class))
                .isInstanceOf(NoDefaultValueException.class);

    }

    public enum TestEnum {
        A, B, C, D
    }

    public enum EmptyEnum {

    }
}
