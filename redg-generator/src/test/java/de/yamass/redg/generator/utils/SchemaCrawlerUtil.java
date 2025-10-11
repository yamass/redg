package de.yamass.redg.generator.utils;

import de.yamass.redg.generator.testutil.DatabaseType;
import de.yamass.redg.generator.testutil.DatabaseTypeTestUtil;
import schemacrawler.schema.*;

import java.util.Optional;
import java.util.stream.Stream;

public class SchemaCrawlerUtil {

	public static Optional<Table> lookupTable(Catalog catalog, DatabaseType databaseType, String tableName) {
		return lookupTables(catalog, databaseType, tableName).findFirst();
	}

	public static Optional<Column> lookupColumn(Catalog catalog, DatabaseType databaseType, String tableName, String columnName) {
		return lookupTables(catalog, databaseType, tableName)
				.flatMap(t -> t.getColumns().stream())
				.filter(column -> column.getName().equalsIgnoreCase(columnName))
				.findFirst();
	}

	public static Optional<ColumnDataType> lookupColumnDataType(Catalog catalog, DatabaseType databaseType, String tableName, String columnName) {
		return lookupColumn(catalog, databaseType, tableName, columnName)
				.map(BaseColumn::getColumnDataType);
	}

	private static Stream<Table> lookupTables(Catalog catalog, DatabaseType databaseType, String tableName) {
		return catalog.getSchemas().stream()
				.filter(schema -> {
					String name = schema.getName() != null ? schema.getName() : schema.getCatalogName();
					return name.equalsIgnoreCase(DatabaseTypeTestUtil.testSchemaName(databaseType));
				})
				.flatMap(s -> catalog.getTables(s).stream())
				.filter(tbale -> tbale.getName().equalsIgnoreCase(tableName));
	}


}
