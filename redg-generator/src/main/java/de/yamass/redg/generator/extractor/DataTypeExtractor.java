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

		return new DataTypeModel(
				columnDataType.getName(),
				columnDataType.getJavaSqlType().getVendorTypeNumber(),
				columnDataType.isNullable()
		);
	}

}
