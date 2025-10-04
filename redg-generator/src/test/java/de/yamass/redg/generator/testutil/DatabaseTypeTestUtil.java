package de.yamass.redg.generator.testutil;

import schemacrawler.inclusionrule.InclusionRule;

public class DatabaseTypeTestUtil {

	public static String testSchemaName(DatabaseType databaseType) {
		return switch (databaseType) {
			case POSTGRES -> "public";
			case MARIADB -> "test";
			case H2 -> "PUBLIC";
			default -> throw new IllegalArgumentException(databaseType + " database type is not supported yet!");
		};
	}

	public static InclusionRule testSchemaInclusionRule(DatabaseType databaseType) {
		return switch (databaseType) {
			case POSTGRES -> "public"::equals;
			case MARIADB -> "test"::equals;
			case H2 -> schemaName -> schemaName.matches(".*\\.PUBLIC$");
			default -> throw new IllegalArgumentException(databaseType + " database type is not supported yet!");
		};
	}

}
