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

package de.yamass.redg.runtime.defaultvalues.pluggable;

import de.yamass.redg.models.ColumnModel;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * The base class for {@link PluggableDefaultValueProvider}s providing dates. Can transform a {@link java.util.Date} into every other JDK time format
 */
public abstract class AbstractDateProvider implements PluggableDefaultValueProvider {

    @Override
    public boolean willProvide(final ColumnModel columnModel) {
        return Date.class.isAssignableFrom(columnModel.getJavaTypeAsClass()) || TemporalAccessor.class.isAssignableFrom(columnModel.getJavaTypeAsClass());
    }

    public static <T> T convertDate(Date date, final Class<T> type) {
        if (Date.class.equals(type)) {
            return type.cast(date);
        } else if (java.sql.Date.class.equals(type)) {
            return type.cast(new java.sql.Date(date.getTime()));
        } else if (Time.class.equals(type)) {
            return type.cast(new Time(date.getTime()));
        } else if (Timestamp.class.equals(type)) {
            return type.cast(new Timestamp(date.getTime()));
        } else if (LocalTime.class.equals(type)) {
            LocalTime t = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalTime();
            return type.cast(t);
        } else if (LocalDate.class.equals(type)) {
            LocalDate d = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
            return type.cast(d);
        } else if (LocalDateTime.class.equals(type)) {
            return type.cast(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        } else if (ZonedDateTime.class.equals(type)) {
            return type.cast(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        } else if (OffsetDateTime.class.equals(type)) {
            return type.cast(OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        } else if (OffsetTime.class.equals(type)) {
            return type.cast(OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toOffsetTime());
        }

        return null;
    }
}
