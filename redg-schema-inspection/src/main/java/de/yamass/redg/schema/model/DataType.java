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

}
