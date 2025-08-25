/*
 * Copyright 2017 BTC Business Technology AG
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

package com.btc.redg.generator.extractor;

import com.btc.redg.generator.extractor.conveniencesetterprovider.ConvenienceSetterProvider;
import com.btc.redg.generator.extractor.conveniencesetterprovider.DefaultConvenienceSetterProvider;
import com.btc.redg.generator.extractor.datatypeprovider.DataTypeProvider;
import com.btc.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import com.btc.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider;
import com.btc.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import com.btc.redg.generator.extractor.nameprovider.DefaultNameProvider;
import com.btc.redg.generator.extractor.nameprovider.NameProvider;
import com.btc.redg.models.ModelUtil;
import com.btc.redg.models.TableModel;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides methods to extract a {@link TableModel} from a {@link Table}. It offers static methods for dealing with join tables as well.
 */
public class TableExtractor {

    public static final String DEFAULT_CLASS_PREFIX = "G";
    public static final String DEFAULT_TARGET_PACKAGE = "com.btc.redg.generated";

    private final String classPrefix;
    private final String targetPackage;
    private final NameProvider nameProvider;

    private final ColumnExtractor columnExtractor;
    private final ForeignKeyExtractor foreignKeyExtractor;

    /**
     * Creates a new table extractor with default configuration.
     */
    public TableExtractor() {
        this(DEFAULT_CLASS_PREFIX, DEFAULT_TARGET_PACKAGE, null, null, null, null);
    }

    /**
     * Creates a new table extractor with the specified class prefix and the specified target package.
     *
     * @param classPrefix               The prefix to put before class names. Only a-zA-Z, starting with a capital letter
     * @param targetPackage             The target package. Usage of default package is forbidden.
     * @param dataTypeProvider          The {@link DataTypeProvider} to read the data types from. If {@code null} defaults to {@link DefaultDataTypeProvider}.
     * @param explicitAttributeDecider  The {@link ExplicitAttributeDecider} that decides whether an attribute or foreign key is "explicit" or not. If
     *                                  {@code null} defaults to {@link DefaultExplicitAttributeDecider}.
     * @param nameProvider              The {@link NameProvider} that provides the class, field and method names for the generated code. If {@code null}
     *                                  defaults to {@link DefaultNameProvider}.
     * @param convenienceSetterProvider The {@link ConvenienceSetterProvider} that returns information about wanted convenience setters. If {@code null}
     *                                  defaults to {@link DefaultConvenienceSetterProvider}.
     */
    public TableExtractor(final String classPrefix, final String targetPackage, DataTypeProvider dataTypeProvider,
                          final NameProvider nameProvider, ExplicitAttributeDecider explicitAttributeDecider,
                          ConvenienceSetterProvider convenienceSetterProvider) {
        Objects.requireNonNull(classPrefix);
        validateClassPrefix(classPrefix);
        this.classPrefix = classPrefix;
        Objects.requireNonNull(targetPackage);
        validateTargetPackage(targetPackage);
        this.targetPackage = targetPackage;

        if (dataTypeProvider == null) {
            dataTypeProvider = new DefaultDataTypeProvider();
        }

        if (nameProvider == null) {
            this.nameProvider = new DefaultNameProvider();
        } else {
            this.nameProvider = nameProvider;
        }

        if (explicitAttributeDecider == null) {
            explicitAttributeDecider = new DefaultExplicitAttributeDecider();
        }

        if (convenienceSetterProvider == null) {
            convenienceSetterProvider = new DefaultConvenienceSetterProvider();
        }

        this.columnExtractor = new ColumnExtractor(dataTypeProvider, this.nameProvider, explicitAttributeDecider, convenienceSetterProvider);
        this.foreignKeyExtractor = new ForeignKeyExtractor(dataTypeProvider, this.nameProvider, explicitAttributeDecider, this.classPrefix);
    }

    private void validateTargetPackage(String targetPackage) {
        if (!targetPackage.matches("^[a-z_][a-z0-9]+(\\.[a-z_][a-z0-9]+)+$")) {
            if (targetPackage.length() == 0) {
                throw new IllegalArgumentException("The default package may not be used.");
            }
            throw new IllegalArgumentException("Package name is invalid. See https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html");
        }
    }

    private void validateClassPrefix(String classPrefix) {
        if (!classPrefix.matches("(^[A-Z][a-zA-Z]*$|^$)")) {
            throw new IllegalArgumentException("Class prefix is invalid. Only a-z is allowed an must start with capital letter");
        }
    }


    /**
     * Extracts the table model from a single table. Every table this table references via foreign keys must be fully loaded, otherwise an exception will be
     * thrown.
     *
     * @param table The table to extract the model from
     * @return The extracted model with all information needed for code generation
     */
    public TableModel extractTableModel(final Table table) {
        Objects.requireNonNull(table);
        final TableModel model = new TableModel();
        model.setClassName(this.classPrefix + this.nameProvider.getClassNameForTable(table));
        model.setName(this.nameProvider.getClassNameForTable(table));
        model.setSqlFullName(table.getFullName());
        model.setSqlName(table.getName());

        model.setPackageName(this.targetPackage);

        model.setColumns(table.getColumns().stream()
                //.filter(c -> !c.isPartOfForeignKey()) // no longer filter due to #12
                .map(this.columnExtractor::extractColumnModel)
                .collect(Collectors.toList()));

        Set<Set<String>> seenForeignKeyColumnNameTuples = new HashSet<>(); // TODO unit test removing duplicates
        model.setForeignKeys(table.getImportedForeignKeys().stream() // only get data about "outgoing" foreign keys
                .filter(foreignKeyColumnReferences -> {
                    Set<String> foreignKeyColumnNames = foreignKeyColumnReferences.getColumnReferences().stream()
                            .map(foreignKeyColumnReference -> foreignKeyColumnReference.getForeignKeyColumn().getFullName())
                            .collect(Collectors.toSet());
                    return seenForeignKeyColumnNameTuples.add(foreignKeyColumnNames);
                })
                .map(this.foreignKeyExtractor::extractForeignKeyModel)
                .collect(Collectors.toList()));


        Set<Set<String>> seenForeignKeyColumnNameTuples2 = new HashSet<>(); // TODO unit test removing duplicates
        model.setIncomingForeignKeys(table.getExportedForeignKeys().stream()
                .filter(foreignKeyColumnReferences -> {
                    Set<String> foreignKeyColumnNames = foreignKeyColumnReferences.getColumnReferences().stream()
                            .map(foreignKeyColumnReference -> foreignKeyColumnReference.getForeignKeyColumn().getFullName())
                            .collect(Collectors.toSet());
                    return seenForeignKeyColumnNameTuples2.add(foreignKeyColumnNames);
                })
                .map(this.foreignKeyExtractor::extractIncomingForeignKeyModel)
                .collect(Collectors.toList()));

        model.setHasColumnsAndForeignKeys(!model.getNonForeignKeyColumns().isEmpty() && !model.getForeignKeys().isEmpty());

        return model;
    }

    /**
     * Uses heuristics to determine whether a table is a join table.
     * <p>
     * A table is identified as a join table if every column is part of a foreign key or part of the primary key. Furthermore the primary may only consist of
     * a single column or every column has to be part of a foreign key.
     *
     * @param table The table to check
     * @return {@code true} if the table is probably a join table, false otherwise
     */
    public static boolean isJoinTable(final Table table) {
        if (table.getPrimaryKey() == null) {
            return false;
        }
        final boolean hasMultipartFK = (table.getPrimaryKey().getConstrainedColumns().size() > 1);
        for (final Column column : table.getColumns()) {
            if (hasMultipartFK) {
                // has to be both
                if (!(column.isPartOfForeignKey() && column.isPartOfPrimaryKey())) {
                    return false;
                }
            } else {
                // If there is a single column that is not part of a foreign key and not the primary key, it needs a separate class
                if (!column.isPartOfForeignKey() && !column.isPartOfPrimaryKey()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Analyzes a join table and returns information about the foreign key relationships in this table
     * <p>
     * Example:<p>
     * If the passed table is table ABC that is a join table for the m:n:o relation between tables A, B and C, the method will return
     * </p>
     * <blockquote><pre>
     *     - A :
     *          - ABC :
     *                 - B
     *                 - C
     *     - B :
     *          - ABC :
     *                 - A
     *                 - C
     *     - C :
     *          - ABC :
     *                 - A
     *                 - B
     * </pre></blockquote>
     * This information should be read as "A has a relation to B and C via the table ABC" and so on
     *
     * @param table The table to analyze
     * @return The information about the join table. See method description for format details.
     */
    public static Map<String, Map<Table, List<String>>> analyzeJoinTable(Table table) {
        final Map<String, Map<Table, List<String>>> result = new HashMap<>();
        for (final ForeignKey fk : table.getForeignKeys()) {

            final String referencedTableName = fk.getColumnReferences().get(0).getPrimaryKeyColumn().getParent().getFullName();
            final Map<Table, List<String>> references = new HashMap<>();
            // Now find all others
            final List<String> otherTables = table.getForeignKeys().stream()
                    .filter(fk2 -> fk2 != fk) // exclude the current one
                    .map(fk2 -> fk2.getColumnReferences().get(0).getPrimaryKeyColumn().getParent().getFullName())
                    .collect(Collectors.toList());
            references.put(table, otherTables);
            result.put(referencedTableName, references);
        }
        return result;
    }
}
