package de.yamass.redg.schema.model;

import java.util.List;

public record ForeignKey(
		Table sourceTable,
		Table targetTable,
		List<ForeignKeyColumn> columns
) {
}
