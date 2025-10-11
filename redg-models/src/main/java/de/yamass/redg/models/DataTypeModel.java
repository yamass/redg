package de.yamass.redg.models;

import java.sql.JDBCType;
import java.util.List;
import java.util.Optional;

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

	/**
	 * Gets the base type of the data type.
	 */
	private final DataTypeModel baseType;

	/**
	 * Gets the parameters needed when using this data type.
	 */
	private final String createParameters;

	/**
	 * Gets the database specific data type name. (Currently always the same as name).
	 */
	private final String databaseSpecificTypeName;

	/**
	 * Get list of enum values if the data type is enumerated.
	 */
	private final List<String> enumValues;

	/**
	 * Gets the literal prefix.
	 */
	private final String literalPrefix;

	/**
	 * Gets the literal suffix.
	 */
	private final String literalSuffix;

	/**
	 * Gets the local (unqualified) data type name.
	 * Note that for standard types, postgres does not return a localTypeName but null.
	 */
	private final String localTypeName;

	/**
	 * Gets the maximum scale.
	 */
	private final int maximumScale;

	/**
	 * Gets the minimum scale.
	 */
	private final int minimumScale;

	/**
	 * Gets the precision of the radix.
	 */
	private final int numPrecisionRadix;

	/**
	 * Gets the precision.
	 */
	private final long precision;

	/**
	 * The java type this data type is typically mapped to through JDBC.
	 * This is not necessarily the type used for code generation!
	 * See schemacrawler.utility.TypeMap
	 */
	private final Class<?> defaultJavaClass;

	private final boolean autoIncrementable;
	private final boolean fixedPrecisionScale;
	private final boolean nullable;
	private final boolean unsigned;

	public DataTypeModel(
			String name,
			String databaseSpecificTypeName,
			String localTypeName,
			String javaSqlTypeName,
			String vendor,
			Integer vendorTypeNumber,
			DataTypeModel baseType,
			String createParameters,
			List<String> enumValues,
			String literalPrefix,
			String literalSuffix,
			int maximumScale,
			int minimumScale,
			int numPrecisionRadix,
			long precision,
			Class<?> typeMappedClass,
			boolean autoIncrementable,
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

	public Optional<JDBCType> getJDBCType() {
		try {
			return Optional.of(JDBCType.valueOf(vendorTypeNumber));
		} catch (Exception e) {
			return Optional.empty();
		}
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
		return !enumValues.isEmpty();
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
