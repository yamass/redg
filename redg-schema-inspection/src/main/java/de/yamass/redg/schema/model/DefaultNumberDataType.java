package de.yamass.redg.schema.model;

import org.jspecify.annotations.Nullable;

import java.sql.JDBCType;

public class DefaultNumberDataType extends DefaultDataType implements NumberDataType {

	private final int maximumScale;
	private final int minimumScale;
	private final int precision;
	private final boolean fixedPrecisionScale;
	private final boolean unsigned;

	public DefaultNumberDataType(String name,
	                             @Nullable JDBCType jdbcType,
	                             Integer typeNumber,
	                             DataType baseType,
	                             boolean autoIncrementable,
	                             int maximumScale,
	                             int minimumScale,
	                             int precision,
	                             boolean fixedPrecisionScale,
	                             boolean unsigned) {
		super(name, jdbcType, typeNumber, baseType, autoIncrementable);
		this.maximumScale = maximumScale;
		this.minimumScale = minimumScale;
		this.precision = precision;
		this.fixedPrecisionScale = fixedPrecisionScale;
		this.unsigned = unsigned;
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

