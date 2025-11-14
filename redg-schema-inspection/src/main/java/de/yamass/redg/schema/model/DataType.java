package de.yamass.redg.schema.model;

import java.sql.JDBCType;
import java.util.Optional;

public interface DataType {

	String getName();

	Optional<JDBCType> getJdbcType();

	/**
	 * See DatabaseMetaData.getTypeInfo()getInt("SOURCE_DATA_TYPE")
	 */
	Integer getTypeNumber();

	DataType getBaseType();

	boolean isAutoIncrementable();

	/**
	 * Returns true if this is an enumerated type (enum).
	 * @return true if this is an enum type
	 */
	default boolean isEnumerated() {
		return !getEnumValues().isEmpty();
	}

	/**
	 * Returns the list of enum values for enumerated types. Returns an empty list for non-enum types.
	 * @return the list of enum values, or an empty list if not an enum
	 */
	default java.util.List<String> getEnumValues() {
		return java.util.Collections.emptyList();
	}

	/**
	 * Returns the number of array dimensions. Returns 0 if this is not an array type.
	 * @return the number of array dimensions, or 0 if not an array
	 */
	int getArrayDimensions();

	/**
	 * Returns true if this is an array type (i.e., getArrayDimensions() > 0).
	 * @return true if this is an array type
	 */
	default boolean isArray() {
		return getArrayDimensions() > 0;
	}

	/**
	 * Returns the maximum scale for numeric types. Returns 0 for non-numeric types.
	 * @return the maximum scale, or 0 if not applicable
	 */
	default int getMaximumScale() {
		return 0;
	}

	/**
	 * Returns the minimum scale for numeric types. Returns 0 for non-numeric types.
	 * @return the minimum scale, or 0 if not applicable
	 */
	default int getMinimumScale() {
		return 0;
	}

	/**
	 * Returns the precision for numeric types. Returns 0 for non-numeric types.
	 * @return the precision, or 0 if not applicable
	 */
	default int getPrecision() {
		return 0;
	}

	/**
	 * Returns true if this numeric type has fixed precision and scale. Returns false for non-numeric types.
	 * @return true if fixed precision/scale, false otherwise
	 */
	default boolean isFixedPrecisionScale() {
		return false;
	}

	/**
	 * Returns true if this numeric type is unsigned. Returns false for non-numeric types.
	 * @return true if unsigned, false otherwise
	 */
	default boolean isUnsigned() {
		return false;
	}

}
