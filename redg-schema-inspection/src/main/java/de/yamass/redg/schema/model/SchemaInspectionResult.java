package de.yamass.redg.schema.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	/**
	 * Combines multiple SchemaInspectionResults into a single result.
	 * Tables, constraints, and UDTs from all results are merged together.
	 * Duplicate tables (same schema and name) are deduplicated, keeping the first occurrence.
	 *
	 * @param results The SchemaInspectionResults to combine
	 * @return A combined SchemaInspectionResult containing all tables, constraints, and UDTs
	 */
	public static SchemaInspectionResult combine(List<SchemaInspectionResult> results) {
		if (results == null || results.isEmpty()) {
			return new SchemaInspectionResult(List.of(), List.of(), List.of());
		}

		// Use LinkedHashMap to preserve order and deduplicate tables by schema+name
		Map<String, Table> tablesMap = new LinkedHashMap<>();
		List<Constraint> allConstraints = new ArrayList<>();
		List<Udt> allUdts = new ArrayList<>();

		for (SchemaInspectionResult result : results) {
			// Add tables, deduplicating by schema+name
			for (Table table : result.tables()) {
				String key = getTableKey(table.schemaName(), table.name());
				tablesMap.putIfAbsent(key, table);
			}

			// Add constraints
			allConstraints.addAll(result.constraints());

			// Add UDTs
			allUdts.addAll(result.udts());
		}

		return new SchemaInspectionResult(
				new ArrayList<>(tablesMap.values()),
				allConstraints,
				allUdts
		);
	}

	private static String getTableKey(String schemaName, String tableName) {
		if (schemaName != null && !schemaName.isEmpty()) {
			return schemaName + "." + tableName;
		}
		return tableName;
	}

	private static boolean equalsIgnoreCase(String left, String right) {
		if (left == null || right == null) {
			return left == null && right == null;
		}
		return left.equalsIgnoreCase(right);
	}
}

