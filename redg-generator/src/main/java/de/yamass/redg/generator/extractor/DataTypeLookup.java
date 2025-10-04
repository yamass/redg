package de.yamass.redg.generator.extractor;

import de.yamass.redg.models.DataTypeModel;
import schemacrawler.schema.ColumnDataType;

public interface DataTypeLookup {

	DataTypeModel getDataTypeModel(ColumnDataType columnDataType);

}
