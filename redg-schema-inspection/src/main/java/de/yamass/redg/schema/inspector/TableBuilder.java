package de.yamass.redg.schema.inspector;

import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class TableBuilder {
	private final QualifiedTableName key;
	private final List<Column> columns = new ArrayList<>();
	private final Map<String, Column> columnsByName = new LinkedHashMap<>();
	private final List<String> primaryKeyColumnNames = new ArrayList<>();
	private Table table;
	private List<ForeignKey> outgoing;
	private List<ForeignKey> incoming;

	TableBuilder(QualifiedTableName key) {
		this.key = key;
	}

	QualifiedTableName key() {
		return key;
	}

	void addColumn(Column column) {
		columns.add(column);
		columnsByName.put(column.name(), column);
	}

	Column column(String name) {
		return columnsByName.get(name);
	}

	void setPrimaryKeyColumnNames(List<String> primaryKeyColumnNames) {
		this.primaryKeyColumnNames.clear();
		this.primaryKeyColumnNames.addAll(primaryKeyColumnNames);
	}

	void initializeTable() {
		this.outgoing = new ArrayList<>();
		this.incoming = new ArrayList<>();
		List<Column> primaryKeyColumns = primaryKeyColumnNames.stream()
				.map(this::column)
				.filter(Objects::nonNull)
				.toList();
		this.table = new Table(key.schema(), key.name(), List.copyOf(columns), primaryKeyColumns, outgoing, incoming);
	}

	Table table() {
		return Objects.requireNonNull(table, "table");
	}

	void addOutgoingForeignKey(ForeignKey foreignKey) {
		outgoing.add(foreignKey);
	}

	void addIncomingForeignKey(ForeignKey foreignKey) {
		incoming.add(foreignKey);
	}
}

