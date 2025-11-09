package de.yamass.redg.schema.model;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ConstraintType {
	PRIMARY_KEY("p"),
	FOREIGN_KEY("f"),
	UNIQUE("u"),
	CHECK("c"),
	TRIGGER("t"),
	EXCLUSION("x"),
	UNKNOWN("");

	private static final Map<String, ConstraintType> BY_CODE = Stream.of(values())
			.collect(Collectors.toUnmodifiableMap(ConstraintType::code, it -> it));

	private final String code;

	ConstraintType(String code) {
		this.code = code;
	}

	public static ConstraintType fromDatabaseCode(String dbCode) {
		if (dbCode == null) {
			return UNKNOWN;
		}
		return BY_CODE.getOrDefault(normalize(dbCode), UNKNOWN);
	}

	private static String normalize(String input) {
		return input.toLowerCase(Locale.ROOT);
	}

	public String code() {
		return code;
	}
}

