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

package de.yamass.redg.runtime.defaultvalues.pluggable.buildermatchers;

import java.util.function.Predicate;

/**
 * Provides conditions like equals, not equals, contains and matchesRegex for usage with the Builder
 */
public class Conditions {

    public static <T> Predicate<T> eq(T value) {
        return value::equals;
    }

    public static <T> Predicate<T> neq(T value) {
        return (d) -> !value.equals(d);
    }

    public static Predicate<String> contains(String value) {
        return (d) -> d.contains(value);
    }

    public static Predicate<String> matchesRegex(String regex) {
        return (d) -> d.matches(regex);
    }

    private Conditions() {

    }
}
