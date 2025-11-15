package de.yamass.redg.models;

public class DataTypeModel implements java.io.Serializable {

	/**
	 * Returns the name that represents a SQL data type.
	 *
	 * @see java.sql.SQLType
	 */
	private final String name;

	/**
	 * Returns the vendor specific type number for the data type.
	 *
	 * @see java.sql.SQLType
	 */
	private final Integer vendorTypeNumber;

	private final boolean nullable;

	public DataTypeModel(
			String name,
			Integer vendorTypeNumber,
			boolean nullable) {
		this.name = name;
		this.vendorTypeNumber = vendorTypeNumber;
		this.nullable = nullable;
	}

	public String getName() {
		return name;
	}

	public Integer getVendorTypeNumber() {
		return vendorTypeNumber;
	}

	public boolean isNullable() {
		return nullable;
	}
}
