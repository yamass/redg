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

import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;

/**
 * This is the interface used for generating the class/method/variable names for the code generation.
 * All methods in this interface must be deterministic. They are allowed to be stateful.
 */
public interface NameProvider {

    /**
     * Generates a class name for a database table.
     * The returned string should adhere to the <a href="https://google.github.io/styleguide/javaguide.html#s5.2.2-class-names">Google Java style Guide</a>
     * and consist solely of alphanumeric characters in UpperCamelCase. While numbers are discouraged, they may be used in the class name,
     * though not at the start of the name.
     * <p><b>This method must be deterministic!</b> Passing the same table multiple times has to yield the same result for each call.</p>
     *
     * @param table The database table
     * @return The generated name
     */
    String getClassNameForTable(Table table);

    /**
     * Generates a method name for a database column.
     * The returned string should adhere to the <a href="https://google.github.io/styleguide/javaguide.html#s5.2.3-method-names">Google Java style Guide</a>
     * and consist solely of alphanumeric characters in lowerCamelCase. While numbers are discouraged, they may be used in the method name,
     * though not at the start of the name.
     * <p><b>This method must be deterministic!</b> Passing the same table multiple times has to yield the same result for each call.</p>
     *
     * @param column The database column
     * @return The generated name
     */
    String getMethodNameForColumn(Column column);

    /**
     * Generates a method name for a database column that represents a foreign key or is part of a foreign key.
     * E.g. the value of "OTHER_ID" column, not the method returning a RedGEntity.
     * The returned string should adhere to the <a href="https://google.github.io/styleguide/javaguide.html#s5.2.3-method-names">Google Java style Guide</a>
     * and consist solely of alphanumeric characters in lowerCamelCase. While numbers are discouraged, they may be used in the method name,
     * though not at the start of the name.
     * <p><b>This method must be deterministic!</b> Passing the same table multiple times has to yield the same result for each call.</p>
     *
     * @param foreignKey The foreign key.
     * @param primaryKeyColumn The referenced database column.
     * @param foreignKeyColumn The referencing database column.
     * @return The generated name
     */
    String getMethodNameForForeignKeyColumn(ForeignKey foreignKey, Column primaryKeyColumn, Column foreignKeyColumn);

    /**
     * Generates a method name for a foreign key.
     * The returned string should adhere to the <a href="https://google.github.io/styleguide/javaguide.html#s5.2.3-method-names">Google Java style Guide</a>
     * and consist solely of alphanumeric characters in lowerCamelCase. While numbers are discouraged, they may be used in the method name,
     * though not at the start of the name.
     * <p><b>This method must be deterministic!</b> Passing the same table multiple times has to yield the same result for each call.</p>
     *
     * @param foreignKey The database foreign key
     * @return The generated name
     */
    String getMethodNameForReference(ForeignKey foreignKey);

    /**
     * Generates a method name for an incoming foreign key.
     * The returned string should adhere to the <a href="https://google.github.io/styleguide/javaguide.html#s5.2.3-method-names">Google Java style Guide</a>
     * and consist solely of alphanumeric characters in lowerCamelCase. While numbers are discouraged, they may be used in the method name,
     * though not at the start of the name.
     * <p><b>This method must be deterministic!</b> Passing the same table multiple times has to yield the same result for each call.</p>
     *
     * @param foreignKey The database foreign key
     * @return The generated name
     */
    String getMethodNameForIncomingForeignKey(ForeignKey foreignKey);
}
