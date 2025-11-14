package de.yamass.redg.schema.model;

import org.jspecify.annotations.Nullable;

import java.sql.JDBCType;
import java.util.Optional;

public class DefaultDataType implements DataType {

	private final String name;
	private final JDBCType jdbcType;
	private final Integer typeNumber;
	private final DataType baseType;
	private final boolean autoIncrementable;
	private final boolean isArray;

	public DefaultDataType(String name, @Nullable JDBCType jdbcType, Integer typeNumber, DataType baseType, boolean autoIncrementable, boolean isArray) {
		this.name = name;
		this.jdbcType = jdbcType;
		this.typeNumber = typeNumber;
		this.baseType = baseType;
		this.autoIncrementable = autoIncrementable;
		this.isArray = isArray;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<JDBCType> getJdbcType() {
		return Optional.ofNullable(jdbcType);
	}

	@Override
	public Integer getTypeNumber() {
		return typeNumber;
	}

	@Override
	public DataType getBaseType() {
		return baseType;
	}

	@Override
	public boolean isAutoIncrementable() {
		return autoIncrementable;
	}

	@Override
	public boolean isEnumerated() {
		return false;
	}

	@Override
	public boolean isArray() {
		return isArray;
	}
}

