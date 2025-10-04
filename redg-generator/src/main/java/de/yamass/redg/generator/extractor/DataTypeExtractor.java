package de.yamass.redg.generator.extractor;

import de.yamass.redg.models.DataTypeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.schema.ColumnDataType;

public class DataTypeExtractor {

	private static final Logger LOG = LoggerFactory.getLogger(ColumnExtractor.class);

	public DataTypeExtractor() {
	}

	public DataTypeModel extractDataType(DataTypeLookup dataTypeLookup, ColumnDataType columnDataType) {
		LOG.debug("Extracting model for data type {}", columnDataType.getName());

		DataTypeModel baseType = columnDataType.getBaseType() != null
				? dataTypeLookup.getDataTypeModel(columnDataType.getBaseType()) : null;

		return new DataTypeModel(
				columnDataType.getName(),
				columnDataType.getJavaSqlType().getName(),
				columnDataType.getJavaSqlType().getVendor(),
				columnDataType.getJavaSqlType().getVendorTypeNumber(),
				baseType,
				columnDataType.getCreateParameters(),
				columnDataType.getDatabaseSpecificTypeName(),
				columnDataType.getEnumValues(),
				columnDataType.getLiteralPrefix(),
				columnDataType.getLiteralSuffix(),
				columnDataType.getLocalTypeName(),
				columnDataType.getMaximumScale(),
				columnDataType.getMinimumScale(),
				columnDataType.getNumPrecisionRadix(),
				columnDataType.getPrecision(),
				columnDataType.getTypeMappedClass(),
				columnDataType.isAutoIncrementable(),
				columnDataType.isEnumerated(),
				columnDataType.isFixedPrecisionScale(),
				columnDataType.isNullable(),
				columnDataType.isUnsigned()
		);
	}

}
