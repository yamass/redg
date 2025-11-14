package de.yamass.redg.schema.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SchemaInspectionResult(
		List<Table> tables,
		List<Constraint> constraints,
		List<Udt> udts
) {

	public SchemaInspectionResult {
		tables = List.copyOf(Objects.requireNonNull(tables, "tables"));
		constraints = List.copyOf(Objects.requireNonNull(constraints, "constraints"));
		udts = List.copyOf(Objects.requireNonNull(udts, "udts"));
	}

	public Optional<Table> findTable(String schemaName, String tableName) {
		if (tableName == null) {
			return Optional.empty();
		}
		return tables.stream()
				.filter(table -> equalsIgnoreCase(schemaName, table.schemaName()) &&
						equalsIgnoreCase(tableName, table.name()))
				.findFirst();
	}

	public Table findTableOrThrow(String schemaName, String tableName) {
		return findTable(schemaName, tableName)
				.orElseThrow(() -> new IllegalStateException("Missing table %s.%s".formatted(schemaName, tableName)));
	}

	private static boolean equalsIgnoreCase(String left, String right) {
		if (left == null || right == null) {
			return left == null && right == null;
		}
		return left.equalsIgnoreCase(right);
	}
}

