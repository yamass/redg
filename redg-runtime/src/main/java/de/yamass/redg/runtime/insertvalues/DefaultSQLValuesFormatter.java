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

package de.yamass.redg.runtime.insertvalues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

/**
 * The default {@link SQLValuesFormatter}. Can format NULL, String/VARCHAR(2), DECIMAL/NUMBER (with Booleans as 1/0)
 * and date types with the TO_TIMESTAMP function. Every unknown type gets inserted with its {@link Object#toString()} representation.
 */
public class DefaultSQLValuesFormatter implements SQLValuesFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSQLValuesFormatter.class);

    @Override
    public <T> String formatValue(final T value, final String sqlDataType, final String fullTableName, final String tableName, final String columnName) {
        if (value == null) {
            return "NULL";
        }
        switch (sqlDataType) {
            case "VARCHAR":
            case "VARCHAR2":
                // varchar has to be wrapped in single quotation marks
                return "'" + value.toString().replace("'", "''") + "'";
            case "DECIMAL":
            case "NUMBER":
                //check is it is a boolean that is being saved as a number
                if (value instanceof Boolean) {
                    return (Boolean) value ? "1" : "0";
                }
                // All java decimals implement a proper toString() method
                return value.toString();

            default:
                break;
        }
        if (value instanceof java.util.Date) {
            Date d = (Date) value;
            Timestamp t = new Timestamp(d.getTime());
            return "TO_TIMESTAMP('" + t.toString() + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
        }
        if (value instanceof TemporalAccessor) {
            TemporalAccessor temporalAccessor = (TemporalAccessor) value;
            String s = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S").format(temporalAccessor);
            return "TO_TIMESTAMP('" + s + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
        }

        LOG.warn("No mapping for {}. Returning result of toString()", sqlDataType);
        return value.toString();
    }
}
