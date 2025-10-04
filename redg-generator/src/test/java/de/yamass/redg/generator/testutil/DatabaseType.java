package de.yamass.redg.generator.testutil;

import org.h2.api.H2Type;

public enum DatabaseType {

	POSTGRES(new DataTypesLookup() {
		@Override
		public String getIntegerType() {
			return "int4";
		}

		@Override
		public String getVarcharType() {
			return "VARCHAR";
		}
	}),
	MARIADB(new DataTypesLookup() {
		@Override
		public String getIntegerType() {
			return "INT";
		}

		@Override
		public String getVarcharType() {
			return "VARCHAR";
		}
	}),
	H2(new DataTypesLookup() {
		@Override
		public String getIntegerType() {
			return H2Type.INTEGER.getName();
		}

		@Override
		public String getVarcharType() {
			return H2Type.VARCHAR.getName();
		}
	}),
	GENERIC(new DataTypesLookup() {
		@Override
		public String getIntegerType() {
			return "INTEGER";
		}

		@Override
		public String getVarcharType() {
			return "VARCHAR";
		}
	});

	private final DataTypesLookup dataTypesLookup;

	DatabaseType(DataTypesLookup dataTypesLookup) {
		this.dataTypesLookup = dataTypesLookup;
	}

	public DataTypesLookup getDataTypesLookup() {
		return dataTypesLookup;
	}
}