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

	boolean isEnumerated();

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

}
