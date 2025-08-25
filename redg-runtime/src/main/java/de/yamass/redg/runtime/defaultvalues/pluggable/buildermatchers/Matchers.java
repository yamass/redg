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

import de.yamass.redg.models.ColumnModel;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * The class containing all the static matchers for the attributes that can be checked with the builder
 */
public class Matchers {

    public static Predicate<ColumnModel> tableName(final Predicate<String> condition) {
        return (cM) -> condition.test(cM.getDbTableName());
    }

    public static Predicate<ColumnModel> fullTableName(final Predicate<String> condition) {
        return (cM) -> condition.test(cM.getDbFullTableName());
    }

    public static Predicate<ColumnModel> columnName(final Predicate<String> condition) {
        return (cM) -> condition.test(cM.getDbName());
    }

    public static Predicate<ColumnModel> type(final Predicate<Class<?>> condition) {
        return (cM) -> condition.test(cM.getJavaTypeAsClass());
    }

    public static Predicate<ColumnModel> notNull(final Predicate<Boolean> condition) {
        return (cM) -> condition.test(cM.isNotNull());
    }

    public static Predicate<ColumnModel> isNotNull() {
        return ColumnModel::isNotNull;
    }

    public static Predicate<ColumnModel> isUnique() {
        return ColumnModel::isUnique;
    }

    public static Predicate<ColumnModel> isPrimary() {
        return ColumnModel::isPartOfPrimaryKey;
    }

    /**
     * A condition that returns true if all of the provided conditions return true. Equivalent to logical AND operator.
     *
     * @param conditions The conditions to be checked
     * @return A condition that returns true if all passed conditions return true
     * @deprecated Use {@link Predicate#and(Predicate)} instead
     */
    @Deprecated
    public static Predicate<ColumnModel> allOf(final Predicate<ColumnModel>... conditions) {
        return (cM) -> Arrays.stream(conditions)
                .allMatch(c -> c.test(cM));
    }

    /**
     * A condition that returns true if any of the provided conditions return true. Equivalent to logical OR operator.
     *
     * @param conditions The conditions to be checked
     * @return A condition that returns true if any of the passed conditions return true
     * @deprecated Use {@link Predicate#or(Predicate)} instead
     */
    @Deprecated
    public static Predicate<ColumnModel> anyOf(final Predicate<ColumnModel>... conditions) {
        return (cM) -> Arrays.stream(conditions)
                .anyMatch(c -> c.test(cM));
    }

    /**
     * A condition that returns true if exactly one of the provided conditions return true. Equivalent to logical XOR operator.
     *
     * @param conditions The conditions to be checked
     * @return A condition that returns true if exactly one of the passed conditions return true
     */
    public static Predicate<ColumnModel> oneOf(final Predicate<ColumnModel>... conditions) {
        return (cM) -> Arrays.stream(conditions)
                .map(c -> c.test(cM)).filter(b -> b).count() == 1;
    }

    private Matchers() {

    }
}
