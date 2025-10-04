package de.yamass.redg.generator.extractor;

import de.yamass.redg.models.DataTypeModel;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AllDataTypesExtractor {

	private final DataTypeExtractor dataTypeExtractor;

	public AllDataTypesExtractor(DataTypeExtractor dataTypeExtractor) {
		this.dataTypeExtractor = dataTypeExtractor;
	}

	public DataTypeLookup extractIntoDataTypeLookup(Catalog catalog) {
		Map<String, DataTypeModel> lookupTable = extractAllDataTypes(catalog);
		return columnDataType -> lookupTable.get(columnDataType.getFullName());
	}

	public Map<String, DataTypeModel> extractAllDataTypes(Catalog catalog) {
		List<Column> columns = catalog.getTables().stream()
				.flatMap(t -> t.getColumns().stream())
				.collect(Collectors.toList());
		Map<String, DataTypeModel> dataTypeModelsByFullSqlName = new HashMap<>();
		for (Column column : columns) {
			extractDataType(dataTypeModelsByFullSqlName, column.getColumnDataType());
		}
		return dataTypeModelsByFullSqlName;
	}

	private DataTypeModel extractDataType(Map<String, DataTypeModel> cache, ColumnDataType columnDataType) {
		DataTypeModel dataType = dataTypeExtractor.extractDataType(cdt -> {
			DataTypeModel cachedDataType = cache.get(cdt.getFullName());
			if (cachedDataType != null) {
				return cachedDataType;
			} else {
				return extractDataType(cache, cdt);
			}
		}, columnDataType);
		cache.put(columnDataType.getFullName(), dataType);
		return dataType;
	}

}
