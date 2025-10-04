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
import de.yamass.redg.models.ColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.schema.Column;

import java.util.Objects;

/**
 * Class that provides a method to extract a {@link ColumnModel} from a {@link ColumnModel}.
 */
public class ColumnExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(ColumnExtractor.class);

	private final DataTypeProvider dataTypeProvider;
	private final NameProvider nameProvider;
	private final ExplicitAttributeDecider explicitAttributeDecider;
	private final ConvenienceSetterProvider convenienceSetterProvider;

	public ColumnExtractor(
			DataTypeProvider dataTypeProvider,
			final NameProvider nameProvider,
			final ExplicitAttributeDecider explicitAttributeDecider,
			final ConvenienceSetterProvider convenienceSetterProvider) {
		this.dataTypeProvider = dataTypeProvider;
		this.explicitAttributeDecider = explicitAttributeDecider;
		this.convenienceSetterProvider = convenienceSetterProvider;
		Objects.requireNonNull(nameProvider);
		this.nameProvider = nameProvider;
	}

	/**
	 * Fills a {@link ColumnModel} with information from a {@link Column}.
	 *
	 * @param column The column
	 * @return The filled model
	 */
	public ColumnModel extractColumnModel(DataTypeLookup dataTypeLookup, Column column) {
		LOG.debug("Extracting model for column {}", column.getName());
		ColumnModel model = new ColumnModel();
		model.setJavaPropertyName(this.nameProvider.getMethodNameForColumn(column));
		model.setDbName(column.getName());
		model.setDbTableName(column.getParent().getName());
		model.setDbFullTableName(column.getParent().getFullName());
		model.setDataType(dataTypeLookup.getDataTypeModel(column.getColumnDataType()));
		String javaTypeName = dataTypeProvider.getCanonicalDataTypeName(column);
		model.setJavaTypeName(javaTypeName);
		model.setNotNull(!column.isNullable());
		model.setPartOfPrimaryKey(column.isPartOfPrimaryKey());
		model.setPartOfForeignKey(column.isPartOfForeignKey());
		model.setExplicitAttribute(explicitAttributeDecider.isExplicitAttribute(column));
		model.setUnique(column.isPartOfUniqueIndex() || column.isPartOfPrimaryKey());
		model.setConvenienceSetters(convenienceSetterProvider.getConvenienceSetters(column, javaTypeName));
		return model;
	}
}
