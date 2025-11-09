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

package de.yamass.redg.extractor;

import de.yamass.redg.extractor.generationmodes.DependencyAlreadyExcludedException;
import de.yamass.redg.extractor.generationmodes.EntityInclusionMode;
import de.yamass.redg.extractor.model.EntityModel;
import de.yamass.redg.extractor.model.ExistingEntityModel;
import de.yamass.redg.extractor.model.ReferencingEntityModel;
import de.yamass.redg.extractor.model.representationprovider.DefaultJavaCodeRepresentationProvider;
import de.yamass.redg.extractor.model.representationprovider.JavaCodeRepresentationProvider;
import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.models.ForeignKeyModel;
import de.yamass.redg.models.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(DataExtractor.class);

    private static final String SELECT_FORMAT_STRING = "SELECT * FROM %s";
    private static final Pattern TABLE_NAME_EXTRACTOR_PATTERN = Pattern.compile(".+\\.(.+)");

    private JavaCodeRepresentationProvider jcrProvider = new DefaultJavaCodeRepresentationProvider();
    private Function<EntityModel, EntityInclusionMode> entityModeDecider = (em) -> EntityInclusionMode.ADD_NEW;

    private String sqlSchemaName = null;

    public JavaCodeRepresentationProvider getJcrProvider() {
        return jcrProvider;
    }

    public void setJcrProvider(final JavaCodeRepresentationProvider jcrProvider) {
        this.jcrProvider = jcrProvider;
    }

    public Function<EntityModel, EntityInclusionMode> getEntityModeDecider() {
        return entityModeDecider;
    }

    public void setEntityModeDecider(final Function<EntityModel, EntityInclusionMode> entityModeDecider) {
        this.entityModeDecider = entityModeDecider;
    }

    public void setSqlSchemaName(String sqlSchemaName) {
        this.sqlSchemaName = sqlSchemaName;
    }

    public List<EntityModel> extractAllData(final DataSource dataSource, final List<TableModel> tableModels) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return extractAllData(connection, tableModels);
        }
    }

    public List<EntityModel> extractAllData(final Connection connection, final List<TableModel> tableModels) throws SQLException {
        final List<EntityModel> entities = extractEntityModels(connection, tableModels);
        resolveReferences(entities);
        return sortEntities(entities);
    }

    private String getFullTableName(final TableModel tm) {
        if (this.sqlSchemaName != null && !this.sqlSchemaName.isEmpty()) {
            // goal: keep escaping of table name if it is escaped in #getSqlFullName()
            // thus we split it with a regex at the last point
            final Matcher tableNameMatcher = TABLE_NAME_EXTRACTOR_PATTERN.matcher(tm.getSqlFullName());
            String escapedTableName;
            if (tableNameMatcher.matches()) {
                escapedTableName = tableNameMatcher.group(1);
            } else {
                // this can only happen if the "full" name did not include a single dot(".")
                // thus, use whole value
                escapedTableName = tm.getSqlFullName();
            }
            
            // H2 has issues with mixed-quoted identifiers like "CATALOG".SCHEMA."table"
            // When the catalog is quoted but schema is not, and table is quoted, H2 fails to parse.
            // Solution: If sqlSchemaName contains a quoted catalog (starts with quote and contains a dot),
            // we need to ensure all parts are consistently quoted.
            // Example: "REDG-EXTRACTOR-SOURCE".PUBLIC."user" should become "REDG-EXTRACTOR-SOURCE"."PUBLIC"."user"
            if (this.sqlSchemaName.startsWith("\"") && this.sqlSchemaName.contains(".")) {
                // Extract catalog and schema from sqlSchemaName (format: "CATALOG".SCHEMA)
                int lastDotIndex = this.sqlSchemaName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    String catalogPart = this.sqlSchemaName.substring(0, lastDotIndex); // "CATALOG"
                    String schemaPart = this.sqlSchemaName.substring(lastDotIndex + 1); // SCHEMA
                    // Ensure table name is quoted if it's a reserved word (like "user")
                    // Quote the schema part for consistency: "CATALOG"."SCHEMA"."table"
                    String quotedTableName = escapedTableName.startsWith("\"") ? escapedTableName : "\"" + escapedTableName + "\"";
                    return catalogPart + ".\"" + schemaPart + "\"." + quotedTableName;
                }
            }
            
            // If sqlSchemaName already contains a dot (like "CATALOG.SCHEMA"), use it as-is
            // Otherwise, add a dot between schema and table name
            if (this.sqlSchemaName.endsWith(".")) {
                return this.sqlSchemaName + escapedTableName;
            } else {
                return this.sqlSchemaName + "." + escapedTableName;
            }
        }
        return tm.getSqlFullName();
    }

    private List<EntityModel> extractEntityModels(Connection connection, List<TableModel> tableModels) throws SQLException {
        final List<EntityModel> entities = new LinkedList<>();
        LOG.debug("Disabling auto commit for connection...");
        connection.setAutoCommit(false);
        LOG.debug("Creating JDBC statement...");

        try (Statement st = connection.createStatement()) {
            LOG.debug("Setting fetch size to 50 rows...");
            st.setFetchSize(50);

            for (final TableModel tableModel : tableModels) {
                String fullTableName = getFullTableName(tableModel);
                LOG.debug("Fetching data from table {}...", fullTableName);
                // Don't use String.format as it might cause issues with quoted identifiers in some cases
                final ResultSet rs = st.executeQuery("SELECT * FROM " + fullTableName);
                long counter = 0;
                while (rs.next()) {
                    ++counter;

                    final EntityModel entityModel = new EntityModel(tableModel);
                    for (final ForeignKeyModel fkm : tableModel.getForeignKeys()) {
                        final EntityModel referencedEntity = new ReferencingEntityModel(fkm.getJavaTypeName());
                        fkm.getReferences().forEach((name, fkcm) -> {
                            try {
                                final Object value = rs.getObject(fkcm.getDbName());
                                if (value != null) {
                                    referencedEntity.addValues(fkcm.getPrimaryKeyAttributeName(), new EntityModel.ValueModel(
                                            jcrProvider.getCodeForColumnValue(value, fkcm.getDbTypeName(), fkcm.getSqlTypeInt(), fkcm.getLocalType()), EntityModel.ValueModel.ForeignKeyState.UNKNOWN));
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        if (fkm.isNotNull()) {
                            entityModel.addNotNullRef(referencedEntity);
                        } else {
                            if (referencedEntity.getValues().size() == fkm.getReferences().size()) {
                                entityModel.addNullableRef(fkm.getJavaPropertyName(), referencedEntity);
                            }
                        }

                    }

                    for (final ColumnModel cm : tableModel.getColumns()) {
                        Object value;
                        try {
                            value = rs.getObject(cm.getDbName());
                        } catch (SQLException e) {
                            // probably name is quoted identifier, remove quotes
                            LOG.warn("Got SQL exception fetching value for column {}", cm.getDbName());
                            LOG.warn("Will try to remove quotes and try again. Consider upgrading to RedG >=2.0 to avoid these issues");
                            value = rs.getObject(cm.getDbName().replace("\"", ""));
                        }
                        if (value != null) {
                            entityModel.addValues(cm.getJavaPropertyName(), new EntityModel.ValueModel(
                                    jcrProvider.getCodeForColumnValue(value, cm.getDbTypeName(), cm.getSqlTypeInt(), cm.getJavaTypeName()),
                                    cm.isPartOfForeignKey() ? EntityModel.ValueModel.ForeignKeyState.FK : EntityModel.ValueModel.ForeignKeyState.NON_FK));
                        }
                    }
                    entities.add(entityModel);

                }
                LOG.debug("Extracted {} entities from table {}", counter, getFullTableName(tableModel));
            }
        }
        return entities;
    }

    private void resolveReferences(List<EntityModel> entities) {
        LOG.debug("Resolving references...");
        for (final EntityModel entity : entities) {
            List<EntityModel> newNotNullRefs = new LinkedList<>();
            for (final EntityModel refEntity : entity.getNotNullRefs()) {
                if (refEntity instanceof ReferencingEntityModel) {
                    newNotNullRefs.add(findCorrectEntity((ReferencingEntityModel) refEntity, entities));
                }
            }
            entity.setNotNullRefs(newNotNullRefs);
            Map<String, EntityModel> newNullableRefs = new HashMap<>();
            entity.getNullableRefs().forEach((name, value) -> {
                if (value instanceof ReferencingEntityModel) {
                    newNullableRefs.put(name, findCorrectEntity((ReferencingEntityModel) value, entities));
                }
            });
            entity.setNullableRefs(newNullableRefs);
        }
    }

    private List<EntityModel> sortEntities(List<EntityModel> entities) {
        LOG.debug("Sorting entities...");
        List<EntityModel> sortedEntities = EntityModelSorter.sortEntityModels(entities);
        ListIterator<EntityModel> sortedIterator = sortedEntities.listIterator();

        List<EntityModel> removedEntities = new LinkedList<>();
        while (sortedIterator.hasNext()) {
            EntityModel entityModel = sortedIterator.next();

            EntityInclusionMode mode = this.entityModeDecider.apply(entityModel);
            switch (mode) {
                case EXCLUDE:
                    removedEntities.add(entityModel);
                    sortedIterator.remove();
                    break;
                case ADD_NEW:
                    for (final EntityModel dependency : entityModel.getNotNullRefs()) {
                        if (removedEntities.contains(dependency)) {
                            throw new DependencyAlreadyExcludedException(entityModel);
                        }
                        entityModel.getNullableRefs().entrySet().removeIf(entry -> removedEntities.contains(entry.getValue()));
                    }
                    break;
                case USE_EXISTING:
                    ExistingEntityModel existingEntityModel = ExistingEntityModel.fromEntityModel(entityModel);
                    sortedIterator.set(existingEntityModel);
                    // replace all references to old entityModel with reference to new existingEntityModel
                    for (final EntityModel eM : sortedEntities) {
                        ListIterator<EntityModel> innerIterator = eM.getNotNullRefs().listIterator();
                        while (innerIterator.hasNext()) {
                            if (innerIterator.next().equals(entityModel)) {
                                innerIterator.set(existingEntityModel);
                            }
                        }
                        eM.setNullableRefs(eM.getNullableRefs().entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> (e.getValue().equals(entityModel)) ? existingEntityModel : e.getValue()
                                )));
                    }
                    break;
            }
        }
        return sortedEntities;
    }

    private EntityModel findCorrectEntity(final ReferencingEntityModel ref, final List<EntityModel> entities) {
        return entities.stream()
                .filter(e -> {
                    if (!ref.getTypeName().equals(e.getTableModel().getClassName())) {
                        return false;
                    }

                    for (Map.Entry<String, EntityModel.ValueModel> refValue : ref.getValues().entrySet()) {
                        String refValueColName = refValue.getKey();
                        EntityModel.ValueModel refValueModel = refValue.getValue();

                        if (!refValueModel.getValue().equals(e.getValues().get(refValueColName).getValue())) {
                            return false;
                        }
                    }

                    return true;
                })
                .findFirst().orElseGet(() -> {
                    LOG.warn("Could not find referenced entity!");
                    return null;
                });
    }


}
