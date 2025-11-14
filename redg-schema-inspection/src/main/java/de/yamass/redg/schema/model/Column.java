package de.yamass.redg.schema.model;

public record Column(
		String name,
		DataType type,
		boolean nullable,
		boolean unique
) {
}
