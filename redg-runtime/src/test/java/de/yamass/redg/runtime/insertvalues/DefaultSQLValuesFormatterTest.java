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

import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.Assert.assertEquals;


public class DefaultSQLValuesFormatterTest {

    @Test
    public void testFormatter() {
        DefaultSQLValuesFormatter formatter = new DefaultSQLValuesFormatter();
        assertEquals("NULL", formatter.formatValue(null, "WHATEVER", "", "", ""));
        assertEquals("'Test'", formatter.formatValue("Test", "VARCHAR2", "", "", ""));
        assertEquals("'Here''s another test'", formatter.formatValue("Here's another test", "VARCHAR2", "", "", ""));
        assertEquals("1234", formatter.formatValue(1234, "DECIMAL", "", "", ""));
        assertEquals("1234.567", formatter.formatValue(1234.567, "DECIMAL", "", "", ""));
        assertEquals("123456789101112", formatter.formatValue(123456789101112L, "DECIMAL", "", "", ""));
        assertEquals("1", formatter.formatValue(true, "DECIMAL", "", "", ""));
        assertEquals("0", formatter.formatValue(false, "DECIMAL", "", "", ""));

        Date date = new Timestamp(448383839000L); // 1984-03-17T15:03:59+00:00
        assertEquals("TO_TIMESTAMP('" + date.toString() + "', 'YYYY-MM-DD HH24:MI:SS.FF')",
                formatter.formatValue(date, "TIMESTAMP", "", "", ""));
        LocalDateTime time = LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
        assertEquals("TO_TIMESTAMP('1984-03-17 15:03:59.0', 'YYYY-MM-DD HH24:MI:SS.FF')",
                formatter.formatValue(time, "TIMESTAMP", "", "", ""));

        Object obj = new Object();
        assertEquals(obj.toString(), formatter.formatValue(obj, "WHATEVER", "", "", ""));

    }
}
