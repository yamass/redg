package de.yamass.redg.generator.extractor;

import schemacrawler.schema.Catalog;

public class ExtractorTestUtil {

	public static DataTypeLookup createDataTypeLookup(Catalog catalog) {
		return new AllDataTypesExtractor(new DataTypeExtractor()).extractIntoDataTypeLookup(catalog);
	}

}
