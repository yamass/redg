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
	private final int arrayDimensions;
	private final int maximumScale;
	private final int minimumScale;
	private final int precision;
	private final boolean fixedPrecisionScale;
	private final boolean unsigned;

	public DefaultDataType(String name, @Nullable JDBCType jdbcType, Integer typeNumber, DataType baseType, boolean autoIncrementable, int arrayDimensions) {
		this(name, jdbcType, typeNumber, baseType, autoIncrementable, arrayDimensions, 0, 0, 0, false, false);
	}

	public DefaultDataType(String name, @Nullable JDBCType jdbcType, Integer typeNumber, DataType baseType, boolean autoIncrementable, int arrayDimensions, int maximumScale, int minimumScale, int precision, boolean fixedPrecisionScale, boolean unsigned) {
		this.name = name;
		this.jdbcType = jdbcType;
		this.typeNumber = typeNumber;
		this.baseType = baseType;
		this.autoIncrementable = autoIncrementable;
		this.arrayDimensions = arrayDimensions;
		this.maximumScale = maximumScale;
		this.minimumScale = minimumScale;
		this.precision = precision;
		this.fixedPrecisionScale = fixedPrecisionScale;
		this.unsigned = unsigned;
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
	public int getArrayDimensions() {
		return arrayDimensions;
	}

	@Override
	public int getMaximumScale() {
		return maximumScale;
	}

	@Override
	public int getMinimumScale() {
		return minimumScale;
	}

	@Override
	public int getPrecision() {
		return precision;
	}

	@Override
	public boolean isFixedPrecisionScale() {
		return fixedPrecisionScale;
	}

	@Override
	public boolean isUnsigned() {
		return unsigned;
	}
}

