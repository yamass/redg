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

package de.yamass.redg.generator.utils;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Maps JDBC types to Java classes, similar to SchemaCrawler's TypeMap utility.
 * This provides the default mapping that SchemaCrawler uses for type mapping.
 */
public class TypeMap {

    private static final Map<JDBCType, Class<?>> TYPE_MAP = new HashMap<>();

    static {
        // Numeric types
        TYPE_MAP.put(JDBCType.BIT, Boolean.class);
        TYPE_MAP.put(JDBCType.TINYINT, Byte.class);
        TYPE_MAP.put(JDBCType.SMALLINT, Short.class);
        TYPE_MAP.put(JDBCType.INTEGER, Integer.class);
        TYPE_MAP.put(JDBCType.BIGINT, Long.class);
        TYPE_MAP.put(JDBCType.FLOAT, Float.class);
        TYPE_MAP.put(JDBCType.REAL, Float.class);
        TYPE_MAP.put(JDBCType.DOUBLE, Double.class);
        TYPE_MAP.put(JDBCType.NUMERIC, BigDecimal.class);
        TYPE_MAP.put(JDBCType.DECIMAL, BigDecimal.class);

        // Character types
        TYPE_MAP.put(JDBCType.CHAR, String.class);
        TYPE_MAP.put(JDBCType.VARCHAR, String.class);
        TYPE_MAP.put(JDBCType.LONGVARCHAR, String.class);
        TYPE_MAP.put(JDBCType.NCHAR, String.class);
        TYPE_MAP.put(JDBCType.NVARCHAR, String.class);
        TYPE_MAP.put(JDBCType.LONGNVARCHAR, String.class);
        TYPE_MAP.put(JDBCType.CLOB, String.class);
        TYPE_MAP.put(JDBCType.NCLOB, String.class);

        // Binary types
        TYPE_MAP.put(JDBCType.BINARY, byte[].class);
        TYPE_MAP.put(JDBCType.VARBINARY, byte[].class);
        TYPE_MAP.put(JDBCType.LONGVARBINARY, byte[].class);
        TYPE_MAP.put(JDBCType.BLOB, byte[].class);

        // Date/Time types
        TYPE_MAP.put(JDBCType.DATE, java.sql.Date.class);
        TYPE_MAP.put(JDBCType.TIME, Time.class);
        TYPE_MAP.put(JDBCType.TIMESTAMP, Timestamp.class);
        TYPE_MAP.put(JDBCType.TIME_WITH_TIMEZONE, OffsetTime.class);
        TYPE_MAP.put(JDBCType.TIMESTAMP_WITH_TIMEZONE, OffsetDateTime.class);

        // Boolean
        TYPE_MAP.put(JDBCType.BOOLEAN, Boolean.class);

        // Other types - matching SchemaCrawler's TypeMap exactly
        TYPE_MAP.put(JDBCType.ARRAY, java.sql.Array.class);
        TYPE_MAP.put(JDBCType.STRUCT, java.sql.Struct.class);
        TYPE_MAP.put(JDBCType.REF, java.sql.Ref.class);
        TYPE_MAP.put(JDBCType.DATALINK, java.net.URL.class);
        TYPE_MAP.put(JDBCType.JAVA_OBJECT, Object.class);
        TYPE_MAP.put(JDBCType.DISTINCT, Object.class);
        TYPE_MAP.put(JDBCType.SQLXML, java.sql.SQLXML.class);
        TYPE_MAP.put(JDBCType.ROWID, java.sql.RowId.class);
        TYPE_MAP.put(JDBCType.REF_CURSOR, Object.class);
    }

    /**
     * Maps a JDBC type to its default Java class.
     * This follows the same mapping rules as SchemaCrawler's TypeMap.
     *
     * @param jdbcType The JDBC type to map
     * @return The Java class that corresponds to the JDBC type, or Object.class if not found
     */
    public static Class<?> getJavaClass(JDBCType jdbcType) {
        return TYPE_MAP.getOrDefault(jdbcType, Object.class);
    }

    /**
     * Maps a JDBC type to its default Java class.
     *
     * @param jdbcType The JDBC type to map (may be null)
     * @return The Java class that corresponds to the JDBC type, or Object.class if not found or null
     */
    public static Class<?> getJavaClass(Optional<JDBCType> jdbcType) {
        if (jdbcType.isEmpty()) {
            return Object.class;
        }
        return getJavaClass(jdbcType.get());
    }

    /**
     * Gets the canonical name of the Java class for a JDBC type.
     *
     * @param jdbcType The JDBC type to map
     * @return The canonical name of the Java class
     */
    public static String getCanonicalName(JDBCType jdbcType) {
        return getJavaClass(jdbcType).getCanonicalName();
    }

    /**
     * Gets the canonical name of the Java class for a JDBC type.
     *
     * @param jdbcType The JDBC type to map (may be null)
     * @return The canonical name of the Java class
     */
    public static String getCanonicalName(Optional<JDBCType> jdbcType) {
        return getJavaClass(jdbcType).getCanonicalName();
    }
}

