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
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class DefaultDefaultValueStrategyTest {

    public static final Map<Class<?>, Object> defaultMappings = new HashMap<>();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    public void testStrategy() {
        DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        ColumnModel columnModel = new ColumnModel();

        columnModel.setNotNull(false);
        assertEquals(null, strategy.getDefaultValue(columnModel, String.class));

        defaultMappings.forEach((key, value) -> {
            final ColumnModel cm = new ColumnModel();
            cm.setNotNull(true);
            cm.setJavaTypeName(key.getName());
            assertEquals(value, strategy.getDefaultValue(cm, key));
        });
    }

    @Test
    public void testStrategy_Primitive() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setJavaTypeName("int");
        Assert.assertEquals(0, new DefaultDefaultValueStrategy().getDefaultValue(cm, int.class).intValue());
    }

    @Test
    public void testStrategy_NoValue() {
        thrown.expect(NoDefaultValueException.class);
        thrown.expectMessage("No default value for type");
        DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        strategy.getDefaultValue(cm, Color.class);
    }

    @Test
    public void testStrategy_UniqueString() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 100; i++) {
            assertEquals(Long.toString(i, 36), strategy.getDefaultValue(cm, String.class));
        }

    }

    @Test
    public void testStrategy_UniqueNumber() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 200; i++) {
            assertEquals(i, (int) strategy.getDefaultValue(cm, Integer.class));
        }
    }

    @Test
    public void testStrategy_UniqueChar() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < Character.MAX_VALUE; i++) {
            assertEquals((char) (i + 1), (char) strategy.getDefaultValue(cm, char.class));
        }
        thrown.expect(NoDefaultValueException.class);
        strategy.getDefaultValue(cm, Character.class);
    }

    @Test
    public void testStrategy_UniqueBoolean() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        assertFalse(strategy.getDefaultValue(cm, boolean.class));
        assertTrue(strategy.getDefaultValue(cm, Boolean.class));

        thrown.expect(NoDefaultValueException.class);
        strategy.getDefaultValue(cm, boolean.class);
    }

    @Test
    public void testStrategy_UniqueDate() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 200; i++) {
            assertEquals(i, strategy.getDefaultValue(cm, Date.class).getTime());
        }
    }

    @Test
    public void testStrategy_UniqueZonedDateTime() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 200; i++) {
            assertEquals(ZonedDateTime.ofInstant(Instant.ofEpochMilli(i), ZoneId.systemDefault()), strategy.getDefaultValue(cm, ZonedDateTime.class));
        }
    }

    @Test
    public void testStrategy_UniqueEnum() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < TestEnum.values().length; i++) {
            assertEquals(TestEnum.values()[i], strategy.getDefaultValue(cm, TestEnum.class));
        }

        thrown.expect(NoDefaultValueException.class);
        strategy.getDefaultValue(cm, TestEnum.class);
    }

    @Test
    public void testStrategy_Enum() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(false);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        for (int i = 0; i < 100; i++) {
            assertEquals(TestEnum.A, strategy.getDefaultValue(cm, TestEnum.class));
        }
    }

    @Test
    public void testStrategy_NoUniquePossible() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();
        thrown.expect(NoDefaultValueException.class);
        strategy.getDefaultValue(cm, Object.class);
    }

    @Test
    public void testStrategy_EmptyUniqueEnum() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(true);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();

        thrown.expect(NoDefaultValueException.class);
        strategy.getDefaultValue(cm, EmptyEnum.class);
    }

    @Test
    public void testStrategy_EmptyEnum() {
        final ColumnModel cm = new ColumnModel();
        cm.setNotNull(true);
        cm.setUnique(false);
        final DefaultDefaultValueStrategy strategy = new DefaultDefaultValueStrategy();

        thrown.expect(NoDefaultValueException.class);
        strategy.getDefaultValue(cm, EmptyEnum.class);
    }

    public enum TestEnum {
        A, B, C, D
    }

    public enum EmptyEnum {

    }
}
