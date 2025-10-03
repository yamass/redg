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

package de.yamass.redg.runtime.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

class DateConverterTest {

    private static final Map<Class<?>, Object> defaultMappings = new HashMap<>();

    static {
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

    private static final OffsetDateTime EXPECTED_DATETIME = OffsetDateTime.of(2017, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
    private static final OffsetDateTime EXPECTED_DATETIME2 = OffsetDateTime.of(2017, 1, 2, 3, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime EXPECTED_DATE = OffsetDateTime.of(2017, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void testParseInstant() throws Exception {
        Assertions.assertEquals(EXPECTED_DATETIME, DateConverter.parseInstant("2017-01-02T03:04:05.000Z", ZoneOffset.UTC).atOffset(ZoneOffset.UTC));
        Assertions.assertEquals(EXPECTED_DATETIME, DateConverter.parseInstant("2017-01-02T03:04:05", ZoneOffset.UTC).atOffset(ZoneOffset.UTC));
        Assertions.assertEquals(EXPECTED_DATE, DateConverter.parseInstant("2017-01-02", ZoneOffset.UTC).atOffset(ZoneOffset.UTC));
        Assertions.assertEquals(EXPECTED_DATETIME2, DateConverter.parseInstant("2017-01-02-03:00", ZoneOffset.UTC).atOffset(ZoneOffset.UTC));
    }

    @Test
    void testParseInstantWithNonIsoDate() throws Exception {
        Assertions.assertEquals(EXPECTED_DATETIME, DateConverter.parseInstant("2017-01-02 03:04:05.000", ZoneOffset.UTC).atOffset(ZoneOffset.UTC));
    }

    @Test
    void testConvertDate() {
        Assertions.assertEquals(new java.util.Date(0), DateConverter.convertDate("1970-01-01T00:00:00Z", java.util.Date.class));
        Assertions.assertEquals(new Date(0), DateConverter.convertDate("1970-01-01T00:00:00Z", Date.class));
        Assertions.assertEquals(new Time(0), DateConverter.convertDate("1970-01-01T00:00:00Z", Time.class));
        Assertions.assertEquals(new Timestamp(0), DateConverter.convertDate("1970-01-01T00:00:00Z", Timestamp.class));
        Assertions.assertEquals(LocalTime.of(0, 0, 0), DateConverter.convertDate("00:00:00", LocalTime.class));
        Assertions.assertEquals(LocalDate.of(1970, 1, 1), DateConverter.convertDate("1970-01-01", LocalDate.class));
        Assertions.assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0), DateConverter.convertDate("1970-01-01T00:00:00", LocalDateTime.class));
        Assertions.assertEquals(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), DateConverter.convertDate("1970-01-01T00:00:00Z", ZonedDateTime.class));
        Assertions.assertEquals(OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), DateConverter.convertDate("1970-01-01T00:00:00Z", OffsetDateTime.class));
        Assertions.assertEquals(OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC), DateConverter.convertDate("00:00:00Z", OffsetTime.class));
        Assertions.assertNull(DateConverter.convertDate("asdf", String.class));

        Assertions.assertEquals(new java.util.Date(0), DateConverter.convertDateFallbackToDefaultTimeZone("1970-01-01T00:00:00Z", java.util.Date.class));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor c = DateConverter.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(c.getModifiers()));

        c.setAccessible(true);
        c.newInstance();
    }

}