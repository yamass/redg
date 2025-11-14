package de.yamass.redg.schema.inspector;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

final class ForeignKeySpec {
	private final QualifiedTableName source;
	private final QualifiedTableName target;
	private final SortedMap<Short, ColumnPair> orderedColumns = new TreeMap<>();

	ForeignKeySpec(QualifiedTableName source, QualifiedTableName target) {
		this.source = source;
		this.target = target;
	}

	void addColumnPair(short sequence, String sourceColumn, String targetColumn) {
		if (sourceColumn == null || targetColumn == null) {
			return;
		}
		orderedColumns.put(sequence, new ColumnPair(sourceColumn, targetColumn));
	}

	QualifiedTableName source() {
		return source;
	}

	QualifiedTableName target() {
		return target;
	}

	List<ColumnPair> columnPairs() {
		return new ArrayList<>(orderedColumns.values());
	}
}

