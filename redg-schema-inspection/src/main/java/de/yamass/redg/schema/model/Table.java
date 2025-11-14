package de.yamass.redg.schema.model;

import java.util.List;
import java.util.Optional;

public record Table(
		String schemaName,
		String name,
		List<Column> columns,
		List<Column> primaryKeyColumns,
		List<ForeignKey> outgoingForeignKeys,
		List<ForeignKey> incomingForeignKeys
) {

	public Optional<Column> findColumn(String columnName) {
		if (columnName == null) {
			return Optional.empty();
		}
		return columns().stream()
				.filter(col -> col.name().equalsIgnoreCase(columnName))
				.findFirst();
	}

	public Column findColumnOrThrow(String columnName) {
		return findColumn(columnName)
				.orElseThrow(() -> new IllegalStateException("Missing column %s on %s".formatted(columnName, name())));
	}

}
