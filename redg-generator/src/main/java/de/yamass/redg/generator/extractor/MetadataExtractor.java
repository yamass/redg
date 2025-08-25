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

import de.yamass.redg.models.ForeignKeyModel;
import de.yamass.redg.models.JoinTableSimplifierModel;
import de.yamass.redg.models.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Table;
import schemacrawler.schema.View;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides methods to extract a complete list of {@link TableModel}s from metadata information provided by schemacrawler.
 * <p>
 * The used {@link TableExtractor} can be specified or the default Table extractor will be used. This class should be used for processing two or more Tables, as
 * it generates helper methods for dealing with m:n relations. When only extracting model data for a single table, use a {@link TableExtractor} directly.
 */
public class MetadataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataExtractor.class);

    /**
     * Extracts a list of {@link TableModel}s from a {@link Catalog} using the specified {@link TableExtractor}.
     * In a post-processing step data for helper methods for dealing with m:n relations/join tables will be added to the models.
     *
     * @param catalog        The catalog containing all the tables that should be processed
     * @param tableExtractor The extractor to use
     * @return A list of table models with exactly one model per table
     */
    public static List<TableModel> extract(final Catalog catalog, final TableExtractor tableExtractor) {
        LOG.info("Extracting model data from {}", catalog.getFullName());

        List<Table> tables = catalog.getTables().stream()
                .filter(table -> !(table instanceof View))
                .collect(Collectors.toList());

        final List<TableModel> result = new LinkedList<>();
        Map<String, Map<Table, List<String>>> joinTableMetadata = new HashMap<>();
        for (Table table : tables) {
            if (TableExtractor.isJoinTable(table)) {
                // Add to the tables that will be post-processed
                LOG.debug("Found join table: {}", table.getFullName());
                joinTableMetadata = MetadataExtractor.mergeJoinTableMetadata(joinTableMetadata, TableExtractor.analyzeJoinTable(table));
            }
            LOG.debug("Extracting model for table {}", table.getFullName());
            result.add(tableExtractor.extractTableModel(table));
        }
        LOG.debug("Post-processing the join tables...");
        processJoinTables(result, joinTableMetadata);
        LOG.debug("Post-processing done.");
        LOG.info("Done extracting the model");
        return result;
    }

    /**
     * Processes the information about the join tables that were collected during table model extraction.
     *
     * @param result The already processed tables
     * @param joinTableMetadata The metadata about the join tables
     */
    private static void processJoinTables(final List<TableModel> result, final Map<String, Map<Table, List<String>>> joinTableMetadata) {
        joinTableMetadata.entrySet().forEach(entry -> {
            LOG.debug("Processing join tables for {}. Found {} join tables to process", entry.getKey(), entry.getValue().size());
            final TableModel model = getModelBySQLName(result, entry.getKey());
            if (model == null) {
                LOG.error("Could not find table {} in the already generated models! This should not happen!", entry.getKey());
                throw new NullPointerException("Table model not found");
            }
            entry.getValue().entrySet().forEach(tableListEntry -> {
                LOG.debug("Processing join table {}", tableListEntry.getKey().getFullName());
                final TableModel joinTable = getModelBySQLName(result, tableListEntry.getKey().getFullName());
                if (joinTable == null) {
                    LOG.error("Could not find join table {} in the already generated models! This should not happen!", entry.getKey());
                    throw new NullPointerException("Table model not found");
                }
                JoinTableSimplifierModel jtsModel = new JoinTableSimplifierModel();
                jtsModel.setName(joinTable.getName());
                for (ForeignKeyModel fKModel : joinTable.getForeignKeys()) {
                    if (fKModel.getJavaTypeName().equals(model.getClassName())) {
                        jtsModel.getConstructorParams().add("this");
                    } else {
                        jtsModel.getConstructorParams().add(fKModel.getName());
                        jtsModel.getMethodParams().put(fKModel.getJavaTypeName(), fKModel.getName());
                    }
                }

                model.getJoinTableSimplifierData().put(joinTable.getClassName(), jtsModel);
            });
        });
    }

    private static TableModel getModelBySQLName(final List<TableModel> set, final String name) {
        return set.stream()
                .filter(tm -> tm.getSqlFullName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Just like {@link #extract(Catalog, TableExtractor)}, just using a default {@link TableExtractor}. Use the other method if you want to specify
     * target Package and  class prefix.
     *
     * @param catalog The catalog containing all the tables that should be processed
     * @return A list of table models with exactly one model per table
     */
    public static List<TableModel> extract(final Catalog catalog) {
        final TableExtractor tableExtractor = new TableExtractor();
        return extract(catalog, tableExtractor);
    }

    /**
     * Performs a deep-merge on the two provided maps, integrating everything from the second map into the first and returning the first map.
     * @param data The first map. Will be modified
     * @param extension The second map, which will be integrated into the first one. Will not be modified
     * @return The resulting map, a reference to the same object that was passed as the first parameter
     */
    private static Map<String, Map<Table, List<String>>> mergeJoinTableMetadata(Map<String, Map<Table, List<String>>> data,
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
}
