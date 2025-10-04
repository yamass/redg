package de.yamass.redg.models.type;

public class StandardType {

	private final String sqlType;
	private final String javaTypeName;
	private final int sqlTypeInt;

	public StandardType(String sqlType, String javaTypeName, int sqlTypeInt) {
		this.sqlType = sqlType;
		this.javaTypeName = javaTypeName;
		this.sqlTypeInt = sqlTypeInt;
	}

	public String getSqlType() {
		return sqlType;
	}

	public String getJavaTypeName() {
		return javaTypeName;
	}

	public int getSqlTypeInt() {
		return sqlTypeInt;
	}

}
