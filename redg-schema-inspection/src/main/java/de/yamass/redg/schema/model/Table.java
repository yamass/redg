package de.yamass.redg.schema.model;

import java.util.List;
import java.util.Optional;

public interface Table {
	String schemaName();
	String name();
	List<Column> columns();
	List<Column> primaryKeyColumns();
	List<ForeignKey> outgoingForeignKeys();
	List<ForeignKey> incomingForeignKeys();

	default Optional<Column> findColumn(String columnName) {
		if (columnName == null) {
			return Optional.empty();
		}
		return columns().stream()
				.filter(col -> col.name().equalsIgnoreCase(columnName))
				.findFirst();
	}

	default Column findColumnOrThrow(String columnName) {
		return findColumn(columnName)
				.orElseThrow(() -> new IllegalStateException("Missing column %s on %s".formatted(columnName, name())));
	}
}
