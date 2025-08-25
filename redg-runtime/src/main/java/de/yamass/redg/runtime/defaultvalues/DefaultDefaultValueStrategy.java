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

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.runtime.defaultvalues.pluggable.AbstractDateProvider;
import de.yamass.redg.runtime.defaultvalues.pluggable.NumberProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple default value strategy, returning empty or 0 values for the most common data types. If the field is nullable, null is returned.
 * <p>
 * Supported types:
 * <ul>
 * <li>String</li>
 * <li>everything extending Number</li>
 * <li>everything extending java.util.Date</li>
 * <li>Boolean</li>
 * </ul>
 */
public class DefaultDefaultValueStrategy implements DefaultValueStrategy {

    private final Logger LOG = LoggerFactory.getLogger(DefaultDefaultValueStrategy.class);

    private static final Map<Class<?>, Object> defaultMappings = new HashMap<>();

    private final Map<String, Long> uniqueCounter = new HashMap<>();

    static {
        defaultMappings.put(String.class, "-");
        defaultMappings.put(Character.class, ' ');
        defaultMappings.put(char.class, ' ');
        defaultMappings.put(Boolean.class, false);
        defaultMappings.put(boolean.class, false);
        // Numbers
        defaultMappings.put(BigDecimal.class, new BigDecimal(0));
        defaultMappings.put(Double.class, 0.0);
        defaultMappings.put(double.class, 0.0);
        defaultMappings.put(Float.class, 0.0f);
        defaultMappings.put(float.class, 0.0f);
        defaultMappings.put(Long.class, 0L);
        defaultMappings.put(long.class, 0L);
        defaultMappings.put(Integer.class, 0);
        defaultMappings.put(int.class, 0);
        defaultMappings.put(Byte.class, (byte) 0);
        defaultMappings.put(byte.class, (byte) 0);
        defaultMappings.put(Short.class, (short) 0);
        defaultMappings.put(short.class, (short) 0);
        defaultMappings.put(AtomicInteger.class, new AtomicInteger(0));
        defaultMappings.put(AtomicLong.class, new AtomicLong(0));
        // SQL Date & Time
        defaultMappings.put(java.util.Date.class, new java.util.Date(0));
        defaultMappings.put(java.sql.Date.class, new java.sql.Date(0));
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

    @Override
    public <T> T getDefaultValue(final ColumnModel columnModel, final Class<T> type) {
        if (!columnModel.isNotNull()) {
            return null;
        }
        if (columnModel.isUnique()) {
            long counter = this.uniqueCounter.compute(
                    columnModel.getDbFullTableName() + "." + columnModel.getDbName(),
                    (k, v) -> (v == null) ? 0L : v + 1);
            if (type.isEnum()) {
                if (type.getEnumConstants().length == 0) {
                    throw new NoDefaultValueException("Cannot pick a value from an empty enum!");
                }
                if (type.getEnumConstants().length <= counter) {
                    throw new NoDefaultValueException("Cannot generate a unique enum value. No more different enums! If this enum is part of a bigger unique index, you cannot use the DefaultDefaultValueService anymore.");
                }
                return type.getEnumConstants()[(int) counter];
            } else {
                return getUniqueValue(counter, type);
            }
        } else {
            if (type.isEnum()) {
                if (type.getEnumConstants().length == 0) {
                    throw new NoDefaultValueException("Cannot pick a value from an empty enum!");
                }
                return type.getEnumConstants()[0];
            } else {
                Object defaultValue = DefaultDefaultValueStrategy.defaultMappings.get(type);
                if (defaultValue != null) {
                    return (T) defaultValue;
                } else {
                    throw new NoDefaultValueException("No default value for type " + type);
                }
            }
        }
    }

    private <T> T getUniqueValue(final long counter, final Class<T> type) {
        if (Number.class.isAssignableFrom(type)) {
            return NumberProvider.convertNumber(new BigDecimal(counter), type);
        }
        if (java.util.Date.class.isAssignableFrom(type) || TemporalAccessor.class.isAssignableFrom(type)) {
            return AbstractDateProvider.convertDate(new java.util.Date(counter), type);
        }
        if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            if (counter > 1)
                throw new NoDefaultValueException("Can only generate 2 unique boolean values. If this boolean is part of a bigger unique index, you cannot use the DefaultDefaultValueService anymore.");
            return (T) new Boolean(counter == 1);
        }
        if (char.class.equals(type) || Character.class.equals(type)) {

            if (counter >= 0xffff)
                throw new NoDefaultValueException("Can only generate 65,535 unique char values. If this char is part of a bigger unique index, you cannot use the DefaultDefaultValueService anymore.");
            return (T) new Character((char) (counter + 1));
        }
        if (String.class.isAssignableFrom(type)) {
            return (T) Long.toString(counter, 36);
        }
        throw new NoDefaultValueException("Could not generate a unique value for type " + type.toString());
    }


}
