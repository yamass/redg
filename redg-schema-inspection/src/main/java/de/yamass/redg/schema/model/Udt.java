package de.yamass.redg.schema.model;

import java.util.Collections;
import java.util.List;

public record Udt(
		String schemaName,
		String name,
		String type,
		String category,
		List<UdtField> fields
) {
	public Udt {
		fields = fields != null ? List.copyOf(fields) : Collections.emptyList();
	}
	
	public Udt(String schemaName, String name, String type, String category) {
		this(schemaName, name, type, category, Collections.emptyList());
	}
}
