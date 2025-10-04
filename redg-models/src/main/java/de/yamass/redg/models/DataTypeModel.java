package de.yamass.redg.models;

import java.util.List;

public class DataTypeModel implements java.io.Serializable {

	/**
	 * Returns the name that represents a SQL data type.
	 *
	 * @see java.sql.SQLType
	 */
	private final String name;

	/**
	 * Returns the name that represents a SQL data type.
	 *
	 * @see java.sql.SQLType
	 */
	private final String javaSqlTypeName;

	/**
	 * Returns the name of the vendor that supports this data type. The value
	 * returned typically is the package name for this vendor.
	 *
	 * @see java.sql.SQLType
	 */
	private final String vendor;

	/**
	 * Returns the vendor specific type number for the data type.
	 *
	 * @see java.sql.SQLType
	 */
	private final Integer vendorTypeNumber;

	private final DataTypeModel baseType;
	private final String createParameters;
	private final String databaseSpecificTypeName;
	private final List<String> enumValues;
	private final String literalPrefix;
	private final String literalSuffix;
	private final String localTypeName;  // TODO remove?
	private final int maximumScale;
	private final int minimumScale;
	private final int numPrecisionRadix;
	private final long precision;

	/**
	 * The java type this data type is typically mapped to through JDBC.
	 * This is not necessarily the type used for code generation!
	 *
	 * @see schemacrawler.utility.TypeMap
	 */
	private final Class<?> defaultJavaClass;

	private final boolean autoIncrementable;
	private final boolean enumerated;
	private final boolean fixedPrecisionScale;
	private final boolean nullable;
	private final boolean unsigned;

	public DataTypeModel(
			String name,
			String javaSqlTypeName,
			String vendor,
			Integer vendorTypeNumber,
			DataTypeModel baseType,
			String createParameters,
			String databaseSpecificTypeName,
			List<String> enumValues,
			String literalPrefix,
			String literalSuffix,
			String localTypeName,
			int maximumScale,
			int minimumScale,
			int numPrecisionRadix,
			long precision,
			Class<?> typeMappedClass,
			boolean autoIncrementable,
			boolean enumerated,
			boolean fixedPrecisionScale,
			boolean nullable,
			boolean unsigned) {
		this.name = name;
		this.javaSqlTypeName = javaSqlTypeName;
		this.vendor = vendor;
		this.vendorTypeNumber = vendorTypeNumber;
		this.baseType = baseType;
		this.createParameters = createParameters;
		this.databaseSpecificTypeName = databaseSpecificTypeName;
		this.enumValues = enumValues;
		this.literalPrefix = literalPrefix;
		this.literalSuffix = literalSuffix;
		this.localTypeName = localTypeName;
		this.maximumScale = maximumScale;
		this.minimumScale = minimumScale;
		this.numPrecisionRadix = numPrecisionRadix;
		this.precision = precision;
		this.defaultJavaClass = typeMappedClass;
		this.autoIncrementable = autoIncrementable;
		this.enumerated = enumerated;
		this.fixedPrecisionScale = fixedPrecisionScale;
		this.nullable = nullable;
		this.unsigned = unsigned;
	}

	public String getName() {
		return name;
	}

	public String getJavaSqlTypeName() {
		return javaSqlTypeName;
	}

	public String getVendor() {
		return vendor;
	}

	public Integer getVendorTypeNumber() {
		return vendorTypeNumber;
	}

	public DataTypeModel getBaseType() {
		return baseType;
	}

	public String getCreateParameters() {
		return createParameters;
	}

	public String getDatabaseSpecificTypeName() {
		return databaseSpecificTypeName;
	}

	public List<String> getEnumValues() {
		return enumValues;
	}

	public String getLiteralPrefix() {
		return literalPrefix;
	}

	public String getLiteralSuffix() {
		return literalSuffix;
	}

	public String getLocalTypeName() {
		return localTypeName;
	}

	public int getMaximumScale() {
		return maximumScale;
	}

	public int getMinimumScale() {
		return minimumScale;
	}

	public int getNumPrecisionRadix() {
		return numPrecisionRadix;
	}

	public long getPrecision() {
		return precision;
	}

	public Class<?> getDefaultJavaClass() {
		return defaultJavaClass;
	}

	public boolean isAutoIncrementable() {
		return autoIncrementable;
	}

	public boolean isEnumerated() {
		return enumerated;
	}

	public boolean isFixedPrecisionScale() {
		return fixedPrecisionScale;
	}

	public boolean isNullable() {
		return nullable;
	}

	public boolean isUnsigned() {
		return unsigned;
	}
}
