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

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.TimeZone;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

@SuppressWarnings("ALL")
public class DateConverter {

    public static final DateFormat ISO8601_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public static final DateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateTimeFormatter ORACLE_LIKE_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(ISO_LOCAL_TIME)
            .toFormatter();

    public static <T> T convertDate(String string, Class<T> type) {
        return convertDateInternal(string, type, ZoneId.of("UTC"));
    }

    public static <T> T convertDateFallbackToDefaultTimeZone(String string, Class<T> type) {
        return convertDateInternal(string, type, TimeZone.getDefault().toZoneId());
    }

    static <T> T convertDateInternal(String string, Class<T> type, ZoneId fallbackZoneId) {
        if (java.util.Date.class.equals(type)) {
            return (T) java.util.Date.from(parseInstant(string, fallbackZoneId));
        } else if (java.sql.Date.class.equals(type)) {
            return (T) java.sql.Date.from(parseInstant(string, fallbackZoneId));
        } else if (Time.class.equals(type)) {
            return (T) Time.from(parseInstant(string, fallbackZoneId));
        } else if (Timestamp.class.equals(type)) {
            return (T) Timestamp.from(parseInstant(string, fallbackZoneId));
        } else if (LocalTime.class.equals(type)) {
            return (T) LocalTime.parse(string);
        } else if (LocalDate.class.equals(type)) {
            return (T) LocalDate.parse(string);
        } else if (LocalDateTime.class.equals(type)) {
            return (T) LocalDateTime.parse(string);
        } else if (ZonedDateTime.class.equals(type)) {
            return (T) ZonedDateTime.parse(string);
        } else if (OffsetDateTime.class.equals(type)) {
            return (T) OffsetDateTime.parse(string);
        } else if (OffsetTime.class.equals(type)) {
            return (T) OffsetTime.parse(string);
        }

        return null;
    }

    static Instant parseInstant(String string, ZoneId fallbackZoneId) {
        if (string.contains("T")) {
            TemporalAccessor temporalAccessor = DateTimeFormatter.ISO_DATE_TIME.parse(string);
            if (temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS)) {
                return Instant.from(temporalAccessor);
            } else {
                return LocalDateTime.from(temporalAccessor).atZone(fallbackZoneId).toInstant();
            }
        } else if (string.contains(" ")) {
            TemporalAccessor temporalAccessor = ORACLE_LIKE_DATE.parse(string);
            return LocalDateTime.from(temporalAccessor).atZone(fallbackZoneId).toInstant();
        } else {
            TemporalAccessor temporalAccessor = DateTimeFormatter.ISO_DATE.parse(string);
            if (temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS)) {
                return LocalDate.from(temporalAccessor).atTime(0, 0).atOffset(ZoneOffset.from(temporalAccessor)).toInstant();
            } else {
                return LocalDate.from(temporalAccessor).atTime(0, 0).atZone(fallbackZoneId).toInstant();
            }
        }
    }

    private DateConverter() {

    }
}
