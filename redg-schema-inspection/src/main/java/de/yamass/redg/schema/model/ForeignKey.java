package de.yamass.redg.schema.model;

import java.util.List;
import java.util.Objects;

public record ForeignKey(
		String name,
		Table sourceTable,
		Table targetTable,
		List<ForeignKeyColumn> columns
) {
	/**
	 * Custom hashCode implementation that uses table qualified names instead of table hash codes.
	 * This prevents infinite recursion when Table and ForeignKey reference each other.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(
				name,
				getTableQualifiedName(sourceTable),
				getTableQualifiedName(targetTable),
				columns
		);
	}

	/**
	 * Custom equals implementation that uses table qualified names instead of table equality.
	 * This prevents infinite recursion when Table and ForeignKey reference each other.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ForeignKey that = (ForeignKey) obj;
		return Objects.equals(name, that.name) &&
				Objects.equals(getTableQualifiedName(sourceTable), getTableQualifiedName(that.sourceTable)) &&
				Objects.equals(getTableQualifiedName(targetTable), getTableQualifiedName(that.targetTable)) &&
				Objects.equals(columns, that.columns);
	}

	private static String getTableQualifiedName(Table table) {
		if (table == null) {
			return null;
		}
		if (table.schemaName() != null && !table.schemaName().isEmpty()) {
			return table.schemaName() + "." + table.name();
		}
		return table.name();
	}
}
