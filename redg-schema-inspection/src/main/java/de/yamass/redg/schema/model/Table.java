package de.yamass.redg.schema.model;

import java.util.List;

public record Table(
		String schemaName,
		String name,
		List<Column> columns,
		List<ForeignKey> outgoingForeignKeys,
		List<ForeignKey> incomingForeignKeys
) {
}
