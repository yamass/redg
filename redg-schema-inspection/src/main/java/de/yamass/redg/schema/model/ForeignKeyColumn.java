package de.yamass.redg.schema.model;

public record ForeignKeyColumn(
		Column sourceColumn,
		Column targetColumn
) {


}
