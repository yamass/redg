package de.yamass.redg.schema.model;

public record Constraint(
		String schemaName,
		String name,
		ConstraintType type,
		String definition,
		boolean partial
) {
}
