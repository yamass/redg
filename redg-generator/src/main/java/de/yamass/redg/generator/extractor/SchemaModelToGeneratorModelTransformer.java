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

package de.yamass.redg.generator.extractor;

import de.yamass.redg.generator.extractor.conveniencesetterprovider.ConvenienceSetterProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.NameProvider;
import de.yamass.redg.models.*;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.DataType;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.ForeignKeyColumn;
import de.yamass.redg.schema.model.SchemaInspectionResult;
import de.yamass.redg.schema.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Transforms a SchemaInspectionResult (from redg-schema-inspection) into a List of TableModel (for redg-generator).
 */
public class SchemaModelToGeneratorModelTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaModelToGeneratorModelTransformer.class);

    private final String classPrefix;
    private final String targetPackage;
    private final DataTypeProvider dataTypeProvider;
    private final NameProvider nameProvider;
    private final ExplicitAttributeDecider explicitAttributeDecider;
    private final ConvenienceSetterProvider convenienceSetterProvider;

    // Cache for DataTypeModel to avoid creating duplicates
    private final Map<String, DataTypeModel> dataTypeModelCache = new HashMap<>();

    public SchemaModelToGeneratorModelTransformer(
            final String classPrefix,
            final String targetPackage,
            final DataTypeProvider dataTypeProvider,
            final NameProvider nameProvider,
            final ExplicitAttributeDecider explicitAttributeDecider,
            final ConvenienceSetterProvider convenienceSetterProvider) {
        this.classPrefix = classPrefix;
        this.targetPackage = targetPackage;
        this.dataTypeProvider = dataTypeProvider;
        this.nameProvider = nameProvider;
        this.explicitAttributeDecider = explicitAttributeDecider;
        this.convenienceSetterProvider = convenienceSetterProvider;
    }

    /**
     * Transforms a SchemaInspectionResult into a list of TableModel objects.
     *
     * @param schemaResult The schema inspection result to transform
     * @return A list of table models ready for code generation
     */
    public List<TableModel> transform(SchemaInspectionResult schemaResult) {
        LOG.info("Transforming schema inspection result to generator model");

        // Build a map of tables by qualified name for quick lookup
        Map<String, Table> tableMap = new HashMap<>();
        for (Table table : schemaResult.tables()) {
            String key = getTableKeyStatic(table.schemaName(), table.name());
            tableMap.put(key, table);
        }

        // Transform all tables
        List<TableModel> result = new ArrayList<>();
        Map<String, Map<Table, List<String>>> joinTableMetadata = new HashMap<>();

        for (Table table : schemaResult.tables()) {
            if (isJoinTable(table)) {
                LOG.debug("Found join table: {}.{}", table.schemaName(), table.name());
                joinTableMetadata = mergeJoinTableMetadata(joinTableMetadata, analyzeJoinTable(table, tableMap));
            }
            LOG.debug("Transforming table {}.{}", table.schemaName(), table.name());
            result.add(transformTable(table));
        }

        // Post-process join tables
        LOG.debug("Post-processing join tables...");
        processJoinTables(result, joinTableMetadata, tableMap);
        LOG.debug("Post-processing done.");

        LOG.info("Transformation complete. Generated {} table models", result.size());
        return result;
    }

    private TableModel transformTable(Table table) {
        TableModel model = new TableModel();
        model.setClassName(this.classPrefix + nameProvider.getClassNameForTable(table));
        model.setName(nameProvider.getClassNameForTable(table));
        model.setSqlFullName(getTableFullName(table));
        model.setSqlName(table.name());
        model.setPackageName(this.targetPackage);

        // Transform columns
        Set<String> primaryKeyColumnNames = table.primaryKeyColumns().stream()
                .map(Column::name)
                .collect(Collectors.toSet());

        Set<String> foreignKeyColumnNames = new HashSet<>();
        for (ForeignKey fk : table.outgoingForeignKeys()) {
            for (ForeignKeyColumn fkCol : fk.columns()) {
                foreignKeyColumnNames.add(fkCol.sourceColumn().name());
            }
        }

        List<ColumnModel> columnModels = new ArrayList<>();
        for (Column column : table.columns()) {
            ColumnModel columnModel = transformColumn(column, table, 
                    primaryKeyColumnNames.contains(column.name()),
                    foreignKeyColumnNames.contains(column.name()));
            columnModels.add(columnModel);
        }
        model.setColumns(columnModels);

        // Transform outgoing foreign keys
        Set<Set<String>> seenForeignKeyColumnNameTuples = new HashSet<>();
        List<ForeignKeyModel> foreignKeyModels = new ArrayList<>();
        for (ForeignKey foreignKey : table.outgoingForeignKeys()) {
            Set<String> fkColumnNames = foreignKey.columns().stream()
                    .map(fkCol -> fkCol.sourceColumn().name())
                    .collect(Collectors.toSet());
            if (seenForeignKeyColumnNameTuples.add(fkColumnNames)) {
                foreignKeyModels.add(transformForeignKey(foreignKey, table));
            }
        }
        // Sort by column order in table
        foreignKeyModels.sort(Comparator.comparing(fk -> {
            String firstColumnName = fk.getReferences().keySet().iterator().next();
            return table.columns().stream()
                    .map(Column::name)
                    .collect(Collectors.toList())
                    .indexOf(firstColumnName);
        }));
        model.setForeignKeys(foreignKeyModels);

        // Transform incoming foreign keys
        Set<Set<String>> seenIncomingForeignKeyColumnNameTuples = new HashSet<>();
        List<IncomingForeignKeyModel> incomingForeignKeyModels = new ArrayList<>();
        for (ForeignKey foreignKey : table.incomingForeignKeys()) {
            Set<String> fkColumnNames = foreignKey.columns().stream()
                    .map(fkCol -> fkCol.targetColumn().name())
                    .collect(Collectors.toSet());
            if (seenIncomingForeignKeyColumnNameTuples.add(fkColumnNames)) {
                incomingForeignKeyModels.add(transformIncomingForeignKey(foreignKey, table));
            }
        }
        model.setIncomingForeignKeys(incomingForeignKeyModels);

        model.setHasColumnsAndForeignKeys(!model.getNonForeignKeyColumns().isEmpty() && !model.getForeignKeys().isEmpty());

        return model;
    }

    private ColumnModel transformColumn(Column column, Table table, boolean isPrimaryKey, boolean isForeignKey) {
        ColumnModel model = new ColumnModel();
        model.setJavaPropertyName(nameProvider.getMethodNameForColumn(column, table));
        model.setDbName(column.name());
        model.setDbTableName(table.name());
        model.setDbFullTableName(getTableFullName(table));
        
        // Transform DataType to DataTypeModel
        DataTypeModel dataTypeModel = getDataTypeModel(column.type());
        model.setDataType(dataTypeModel);
        
        // Get Java type name from DataTypeProvider
        String javaTypeName = dataTypeProvider.getCanonicalDataTypeName(column, table);
        model.setJavaTypeName(javaTypeName);
        
        model.setNotNull(!column.nullable());
        model.setPartOfPrimaryKey(isPrimaryKey);
        model.setPartOfForeignKey(isForeignKey);
        model.setExplicitAttribute(explicitAttributeDecider.isExplicitAttribute(column, table));
        model.setUnique(column.unique() || isPrimaryKey);
        model.setConvenienceSetters(convenienceSetterProvider.getConvenienceSetters(column, table, javaTypeName));
        
        return model;
    }

    private ForeignKeyModel transformForeignKey(ForeignKey foreignKey, Table sourceTable) {
        ForeignKeyModel model = new ForeignKeyModel();
        Table targetTable = foreignKey.targetTable();
        
        model.setJavaTypeName(this.classPrefix + nameProvider.getClassNameForTable(targetTable));
        model.setJavaPropertyName(nameProvider.getMethodNameForReference(foreignKey));
        
        // Check if any column in the foreign key is nullable
        boolean isNotNull = foreignKey.columns().stream()
                .noneMatch(fkCol -> fkCol.sourceColumn().nullable())
                || explicitAttributeDecider.isExplicitForeignKey(foreignKey);
        model.setNotNull(isNotNull);

        // Transform foreign key columns
        Map<String, ForeignKeyColumnModel> references = new HashMap<>();
        for (ForeignKeyColumn fkColumn : foreignKey.columns()) {
            Column sourceColumn = fkColumn.sourceColumn();
            Column targetColumn = fkColumn.targetColumn();
            
            ForeignKeyColumnModel fkColumnModel = new ForeignKeyColumnModel();
            fkColumnModel.setPrimaryKeyAttributeName(nameProvider.getMethodNameForColumn(targetColumn, foreignKey.targetTable()));
            fkColumnModel.setLocalName(nameProvider.getMethodNameForForeignKeyColumn(fkColumn, sourceTable));
            
            fkColumnModel.setLocalType(dataTypeProvider.getCanonicalDataTypeName(sourceColumn, sourceTable));
            fkColumnModel.setDbTypeName(sourceColumn.type().getName());
            fkColumnModel.setSqlTypeInt(sourceColumn.type().getTypeNumber() != null 
                    ? sourceColumn.type().getTypeNumber() : 0);
            
            fkColumnModel.setDbName(sourceColumn.name());
            fkColumnModel.setDbTableName(sourceTable.name());
            fkColumnModel.setDbFullTableName(getTableFullName(sourceTable));
            
            references.put(sourceColumn.name(), fkColumnModel);
        }
        model.getReferences().putAll(references);
        
        return model;
    }

    private IncomingForeignKeyModel transformIncomingForeignKey(ForeignKey foreignKey, Table targetTable) {
        IncomingForeignKeyModel model = new IncomingForeignKeyModel();
        Table sourceTable = foreignKey.sourceTable();
        
        model.setReferencingJavaTypeName(this.classPrefix + nameProvider.getClassNameForTable(sourceTable));
        model.setReferencingAttributeName(nameProvider.getMethodNameForReference(foreignKey));
        model.setAttributeName(nameProvider.getMethodNameForIncomingForeignKey(foreignKey));
        
        boolean isNotNull = foreignKey.columns().stream()
                .noneMatch(fkCol -> fkCol.sourceColumn().nullable())
                || explicitAttributeDecider.isExplicitForeignKey(foreignKey);
        model.setNotNull(isNotNull);
        
        return model;
    }

    private DataTypeModel getDataTypeModel(DataType dataType) {
        String key = dataType.getName() + ":" + 
                (dataType.getTypeNumber() != null ? dataType.getTypeNumber() : "null");
        return dataTypeModelCache.computeIfAbsent(key, k -> {
            return new DataTypeModel(
                    dataType.getName(),
                    dataType.getTypeNumber(),
                    true // nullable - this is a type property, not column property
            );
        });
    }

    private String getTableFullName(Table table) {
        if (table.schemaName() != null && !table.schemaName().isEmpty()) {
            return table.schemaName() + "." + table.name();
        }
        return table.name();
    }

    // Join table detection and processing
    private static boolean isJoinTable(Table table) {
        if (table.primaryKeyColumns().isEmpty()) {
            return false;
        }
        boolean hasMultipartPK = table.primaryKeyColumns().size() > 1;
        for (Column column : table.columns()) {
            boolean isPK = table.primaryKeyColumns().contains(column);
            boolean isFK = table.outgoingForeignKeys().stream()
                    .anyMatch(fk -> fk.columns().stream()
                            .anyMatch(fkCol -> fkCol.sourceColumn().equals(column)));
            if (hasMultipartPK) {
                if (!(isPK && isFK)) {
                    return false;
                }
            } else {
                if (!isPK && !isFK) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Map<String, Map<Table, List<String>>> analyzeJoinTable(Table joinTable, Map<String, Table> tableMap) {
        Map<String, Map<Table, List<String>>> result = new HashMap<>();
        for (ForeignKey fk : joinTable.outgoingForeignKeys()) {
            Table referencedTable = fk.targetTable();
            String referencedTableKey = getTableKeyStatic(referencedTable.schemaName(), referencedTable.name());
            Map<Table, List<String>> references = new HashMap<>();
            List<String> otherTables = joinTable.outgoingForeignKeys().stream()
                    .filter(fk2 -> fk2 != fk)
                    .map(fk2 -> getTableKeyStatic(fk2.targetTable().schemaName(), fk2.targetTable().name()))
                    .collect(Collectors.toList());
            references.put(joinTable, otherTables);
            result.put(referencedTableKey, references);
        }
        return result;
    }

    private static String getTableKeyStatic(String schemaName, String tableName) {
        if (schemaName != null && !schemaName.isEmpty()) {
            return schemaName + "." + tableName;
        }
        return tableName;
    }

    private static Map<String, Map<Table, List<String>>> mergeJoinTableMetadata(
            Map<String, Map<Table, List<String>>> data,
            Map<String, Map<Table, List<String>>> extension) {
        for (String key : extension.keySet()) {
            Map<Table, List<String>> dataForTable = data.get(key);
            if (dataForTable == null) {
                data.put(key, extension.get(key));
                continue;
            }
            for (Table t : extension.get(key).keySet()) {
                dataForTable.put(t, extension.get(key).get(t));
            }
            data.put(key, dataForTable);
        }
        return data;
    }

    private void processJoinTables(List<TableModel> result, Map<String, Map<Table, List<String>>> joinTableMetadata, Map<String, Table> tableMap) {
        joinTableMetadata.entrySet().forEach(entry -> {
            LOG.debug("Processing join tables for {}. Found {} join tables to process", entry.getKey(), entry.getValue().size());
            TableModel model = getModelBySQLName(result, entry.getKey());
            if (model == null) {
                LOG.error("Could not find table {} in the already generated models! This should not happen!", entry.getKey());
                throw new NullPointerException("Table model not found");
            }
            entry.getValue().entrySet().forEach(tableListEntry -> {
                LOG.debug("Processing join table {}.{}", tableListEntry.getKey().schemaName(), tableListEntry.getKey().name());
                TableModel joinTable = getModelBySQLName(result, getTableFullName(tableListEntry.getKey()));
                if (joinTable == null) {
                    LOG.error("Could not find join table {} in the already generated models! This should not happen!", entry.getKey());
                    throw new NullPointerException("Table model not found");
                }
                JoinTableSimplifierModel jtsModel = new JoinTableSimplifierModel();
                jtsModel.setName(joinTable.getName());
                for (ForeignKeyModel fkModel : joinTable.getForeignKeys()) {
                    if (fkModel.getJavaTypeName().equals(model.getClassName())) {
                        jtsModel.getConstructorParams().add("this");
                    } else {
                        jtsModel.getConstructorParams().add(fkModel.getJavaPropertyName());
                        jtsModel.getMethodParams().put(fkModel.getJavaTypeName(), fkModel.getJavaPropertyName());
                    }
                }
                model.getJoinTableSimplifierData().put(joinTable.getClassName(), jtsModel);
            });
        });
    }

    private static TableModel getModelBySQLName(List<TableModel> models, String name) {
        return models.stream()
                .filter(tm -> tm.getSqlFullName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
