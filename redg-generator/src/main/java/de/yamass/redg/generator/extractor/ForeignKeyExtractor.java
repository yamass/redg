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

import de.yamass.redg.generator.exceptions.RedGGenerationException;
import de.yamass.redg.generator.extractor.datatypeprovider.DataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.NameProvider;
import de.yamass.redg.models.ForeignKeyColumnModel;
import de.yamass.redg.models.ForeignKeyModel;
import de.yamass.redg.models.IncomingForeignKeyModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.crawl.NotLoadedException;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;

import java.util.Objects;

/**
 * Provides a method to extract model data about a single foreign key.
 */
public class ForeignKeyExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(ForeignKeyExtractor.class);

    private final String classPrefix;
    private final DataTypeProvider dataTypeProvider;
    private final NameProvider nameProvider;
    private final ExplicitAttributeDecider explicitAttributeDecider;

    public ForeignKeyExtractor(final DataTypeProvider dataTypeProvider, final NameProvider nameProvider,
                               final ExplicitAttributeDecider explicitAttributeDecider, final String classPrefix) {
        Objects.requireNonNull(classPrefix);
        Objects.requireNonNull(dataTypeProvider);
        Objects.requireNonNull(nameProvider);
        Objects.requireNonNull(explicitAttributeDecider);
        this.classPrefix = classPrefix;
        this.dataTypeProvider = dataTypeProvider;
        this.nameProvider = nameProvider;
        this.explicitAttributeDecider = explicitAttributeDecider;
    }

    /**
     * Extracts a {@link ForeignKeyModel} from a foreign key in the database. The {@link ForeignKeyModel} contains some general information and
     * detailed information about each column that is part of the foreign key.
     * <p>
     * Throws an {@link RedGGenerationException} if the referenced table is only partially loaded (because it has been excluded by a filter)
     *
     * @param foreignKey The foreign key
     * @return The model, filled with the extracted information
     */
    public ForeignKeyModel extractForeignKeyModel(final ForeignKey foreignKey) {
        LOG.debug("Extracting model for foreign key {}", foreignKey.getName());
        final ForeignKeyModel model = new ForeignKeyModel();
        final Table targetTable = foreignKey.getColumnReferences().get(0).getPrimaryKeyColumn().getParent();
        final Table originTable = foreignKey.getColumnReferences().get(0).getForeignKeyColumn().getParent();
        try {
            targetTable.hasPrimaryKey();
        } catch (NotLoadedException e) {
            LOG.error("Foreign key reference on an excluded table {}. Cannot generate valid java code.", targetTable.getFullName());
            throw new RedGGenerationException("Referenced foreign key is in an excluded table: " + targetTable.getFullName());
        }
        model.setJavaTypeName(this.classPrefix + this.nameProvider.getClassNameForTable(targetTable));
        model.setJavaPropertyName(this.nameProvider.getMethodNameForReference(foreignKey));
        model.setNotNull(foreignKey.getColumnReferences().stream().noneMatch(x -> x.getForeignKeyColumn().isNullable())
                || explicitAttributeDecider.isExplicitForeignKey(foreignKey));

        for (final ColumnReference reference : foreignKey.getColumnReferences()) {
            LOG.debug("{}: {} -> {}", foreignKey.getName(), reference.getForeignKeyColumn().getName(), reference.getPrimaryKeyColumn().getName());
            final ForeignKeyColumnModel columnModel = new ForeignKeyColumnModel();
            columnModel.setPrimaryKeyAttributeName(this.nameProvider.getMethodNameForColumn(reference.getPrimaryKeyColumn()));
            columnModel.setLocalName(this.nameProvider.getMethodNameForForeignKeyColumn(foreignKey, reference.getPrimaryKeyColumn(), reference.getForeignKeyColumn()));
            columnModel.setLocalType(dataTypeProvider.getCanonicalDataTypeName(reference.getForeignKeyColumn()));
            columnModel.setDbTypeName(reference.getForeignKeyColumn().getColumnDataType().getName());
            columnModel.setSqlTypeInt(reference.getForeignKeyColumn().getColumnDataType().getJavaSqlType().getVendorTypeNumber());

            columnModel.setDbName(reference.getForeignKeyColumn().getName());
            columnModel.setDbTableName(originTable.getName());
            columnModel.setDbFullTableName(originTable.getFullName());
            model.getReferences().put(reference.getForeignKeyColumn().getName(), columnModel);
        }
        return model;
    }

    public IncomingForeignKeyModel extractIncomingForeignKeyModel(final ForeignKey foreignKey) {
        LOG.debug("Extracting model for incoming foreign key {}", foreignKey.getName());
        final IncomingForeignKeyModel model = new IncomingForeignKeyModel();
        final Table targetTable = foreignKey.getColumnReferences().get(0).getPrimaryKeyColumn().getParent();
        final Table originTable = foreignKey.getColumnReferences().get(0).getForeignKeyColumn().getParent();
        try {
            originTable.hasPrimaryKey();
        } catch (NotLoadedException e) {
            LOG.error("Foreign key reference from an excluded table {}. Cannot generate valid java code.", originTable.getFullName());
            throw new RedGGenerationException("Referencing foreign key is in an excluded table: " + originTable.getFullName());
        }
        model.setReferencingJavaTypeName(this.classPrefix + this.nameProvider.getClassNameForTable(originTable));
        model.setReferencingEntityName(this.nameProvider.getClassNameForTable(originTable));
        model.setReferencingAttributeName(this.nameProvider.getMethodNameForReference(foreignKey));
        model.setAttributeName(this.nameProvider.getMethodNameForIncomingForeignKey(foreignKey));
        model.setNotNull(!foreignKey.getColumnReferences().get(0).getForeignKeyColumn().isNullable()
                || explicitAttributeDecider.isExplicitForeignKey(foreignKey));

        return model;
    }

}
