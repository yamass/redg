package de.yamass.redg.generator.testutil;

import de.yamass.redg.DatabaseType;
import org.h2.api.H2Type;
import schemacrawler.inclusionrule.InclusionRule;

public class DatabaseTypeTestUtil {

	public static InclusionRule testSchemaInclusionRule(DatabaseType databaseType) {
		return switch (databaseType) {
			case POSTGRES -> "public"::equals;
			case MARIADB -> "test"::equals;
			case H2 -> schemaName -> schemaName.matches(".*\\.PUBLIC$");
			default -> throw new IllegalArgumentException(databaseType + " database type is not supported yet!");
		};
	}

	public static String getIntegerType(DatabaseType databaseType) {
		return switch (databaseType) {
			case POSTGRES -> "int4";
			case MARIADB -> "INT";
			case H2 -> H2Type.INTEGER.getName();
			case GENERIC -> "INTEGER";
		};
	}

	public static String getVarcharType(DatabaseType databaseType) {
		return switch (databaseType) {
			case POSTGRES -> "VARCHAR";
			case MARIADB -> "VARCHAR";
			case H2 -> H2Type.VARCHAR.getName();
			case GENERIC -> "VARCHAR";
		};
	}
}
