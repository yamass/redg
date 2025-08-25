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

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class NameUtilsTest {

    @Test
    public void testFirstCharacterToLowerCase() {
        assertEquals("hello", NameUtils.firstCharacterToLowerCase("hello"));
        assertEquals("hello", NameUtils.firstCharacterToLowerCase("Hello"));
        assertEquals("hELLO", NameUtils.firstCharacterToLowerCase("HELLO"));
        assertEquals("4heLLo", NameUtils.firstCharacterToLowerCase("4heLLo"));
        assertEquals("", NameUtils.firstCharacterToLowerCase(""));
        assertEquals(null, NameUtils.firstCharacterToLowerCase(null));
    }

    @Test
    public void testFirstCharacterToUpperCase() {
        assertEquals("Hello", NameUtils.firstCharacterToUpperCase("hello"));
        assertEquals("Hello", NameUtils.firstCharacterToUpperCase("Hello"));
        assertEquals("HELLO", NameUtils.firstCharacterToUpperCase("HELLO"));
        assertEquals("4heLLo", NameUtils.firstCharacterToUpperCase("4heLLo"));
        assertEquals("", NameUtils.firstCharacterToUpperCase(""));
        assertEquals(null, NameUtils.firstCharacterToUpperCase(null));
    }

}
