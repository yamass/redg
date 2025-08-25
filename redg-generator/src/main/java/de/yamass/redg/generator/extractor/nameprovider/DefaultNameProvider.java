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

package de.yamass.redg.generator.extractor.nameprovider;

import de.yamass.redg.generator.utils.NameUtils;

import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The default name provider for RedG
 */
public class DefaultNameProvider implements NameProvider {

    /**
     * Turns a SQL table by its name into a java class name (upper-case camel-case).
     * <p>
     * Example: WEB_USERS -&gt; WebUsers
     * @param table The database table
     * @return The java class name
     */
    @Override
    public String getClassNameForTable(final Table table) {
        final StringBuilder classNameBuilder = new StringBuilder();
        final List<String> words = new ArrayList<>(Arrays.asList(table.getName()
                .toUpperCase()
                .replaceAll("(^[0-9]+|[^A-Z0-9_-])", "") // Delete every not-alphanumeric or _/- character and numbers at beginning
                .split("_")));
        words.removeAll(Arrays.asList("", null));
        for (String word : words) {
            classNameBuilder.append(word.substring(0, 1)); // First letter as uppercase
            classNameBuilder.append(word.substring(1).toLowerCase()); // Remaining string as lowercase
        }

        return classNameBuilder.toString();
    }

    /**
     * Turns a SQL column name into a java method name (lower-case camel-case).
     * <p>
     * Example: FIRST_NAME -&gt; firstName
     *
     * @param column The database column
     * @return The method name
     */
    @Override
    public String getMethodNameForColumn(final Column column) {
        return convertToJavaName(column.getName());
    }

    public static String convertToJavaName(String columnName) {
        final StringBuilder methodNameBuilder = new StringBuilder();
        final List<String> words = new ArrayList<>(Arrays.asList(columnName
                .toLowerCase()
                .replaceAll("(^[0-9]+|[^a-z0-9_-])", "") // Delete every not-alphanumeric or _/- character and numbers at beginning
                .split("_")));
        words.removeAll(Arrays.asList("", null));
        methodNameBuilder.append(words.get(0)); //First part starts lower case
        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);

            methodNameBuilder.append(word.substring(0, 1).toUpperCase());
            methodNameBuilder.append(word.substring(1));
        }
        return methodNameBuilder.toString();
    }

    @Override
    public String getMethodNameForForeignKeyColumn(ForeignKey foreignKey, Column primaryKeyColumn, Column foreignKeyColumn) {
        return getMethodNameForColumn(foreignKeyColumn);
    }

    /**
     * Generates an appropriate method name for a foreign key
     * @param foreignKey The database foreign key
     * @return The generated name
     */
    @Override
    public String getMethodNameForReference(final ForeignKey foreignKey) {
        final Column c = foreignKey.getColumnReferences().get(0).getForeignKeyColumn();
        // check if only one-column fk
        if (foreignKey.getColumnReferences().size() == 1) {
            return getMethodNameForColumn(c) + getClassNameForTable(c.getReferencedColumn().getParent());
        }

        final StringBuilder nameBuilder = new StringBuilder();
        final List<String> words = new ArrayList<>(Arrays.asList(foreignKey.getName()
                .toLowerCase()
                .replaceAll("(^[0-9]+|[^a-z0-9_-])", "") // Delete every not-alphanumeric or _/- character and numbers at beginning
                .split("_")));
        words.removeAll(Arrays.asList("fk", "", null)); // removes FK_ prefix and empty entries
        final List<String> tableWords = new ArrayList<>(Arrays.asList(c.getParent().getName()
                .toLowerCase()
                .replaceAll("(^[0-9]+|[^a-z0-9_-])", "") // Delete every not-alphanumeric or _/- character and numbers at beginning
                .split("_")));
        words.removeAll(tableWords);
        nameBuilder.append(words.get(0));
        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);

            nameBuilder.append(word.substring(0, 1).toUpperCase());
            nameBuilder.append(word.substring(1));
        }
        return nameBuilder.toString();
    }

    @Override
    public String getMethodNameForIncomingForeignKey(ForeignKey foreignKey) {
        Table referencingTable = foreignKey.getColumnReferences().get(0).getForeignKeyColumn().getParent();
        return NameUtils.firstCharacterToLowerCase(getClassNameForTable(referencingTable))
                + "sFor" + NameUtils.firstCharacterToUpperCase(getMethodNameForReference(foreignKey));
    }
}
