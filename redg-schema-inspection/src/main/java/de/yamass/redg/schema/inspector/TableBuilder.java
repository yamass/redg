package de.yamass.redg.schema.inspector;

import de.yamass.redg.schema.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class TableBuilder {
	private final QualifiedTableName key;
	private final List<ColumnMetadata> columnMetadataList = new ArrayList<>();
	private final List<String> primaryKeyColumnNames = new ArrayList<>();
	private MutableTable table;

	TableBuilder(QualifiedTableName key) {
		this.key = key;
	}

	QualifiedTableName key() {
		return key;
	}

	void addColumnMetadata(String name, DataType type, boolean nullable, boolean unique) {
		ColumnMetadata metadata = new ColumnMetadata(name, type, nullable, unique);
		columnMetadataList.add(metadata);
	}

	void setPrimaryKeyColumnNames(List<String> primaryKeyColumnNames) {
		this.primaryKeyColumnNames.clear();
		this.primaryKeyColumnNames.addAll(primaryKeyColumnNames);
	}

	void initializeTable() {
		// Create table first with empty columns
		this.table = new MutableTable(key.schema(), key.name());
		
		// Create Column records with table reference and add them to the table
		for (ColumnMetadata metadata : columnMetadataList) {
			Column column = new Column(metadata.name(), metadata.type(), metadata.nullable(), metadata.unique(), table);
			table.addColumn(column);
		}
		
		// Set primary key columns (reuse columns already added to the table)
		for (String pkColumnName : primaryKeyColumnNames) {
			table.columns().stream()
					.filter(c -> c.name().equals(pkColumnName))
					.findFirst()
					.ifPresent(pkColumn -> table.addPrimaryKeyColumn(pkColumn));
		}
	}

	Table table() {
		return Objects.requireNonNull(table, "table");
	}

	void addOutgoingForeignKey(ForeignKey foreignKey) {
		table.addOutgoingForeignKey(foreignKey);
	}

	void addIncomingForeignKey(ForeignKey foreignKey) {
		table.addIncomingForeignKey(foreignKey);
	}

	/**
	 * Internal class to hold column metadata before the table is created.
	 */
	private record ColumnMetadata(String name, DataType type, boolean nullable, boolean unique) {
	}
}
