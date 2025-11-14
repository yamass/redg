package de.yamass.redg.schema.model;

public interface NumberDataType extends DataType {

	int getMaximumScale();

	int getMinimumScale();

	int getPrecision();

	boolean isFixedPrecisionScale();

	boolean isUnsigned();

}
