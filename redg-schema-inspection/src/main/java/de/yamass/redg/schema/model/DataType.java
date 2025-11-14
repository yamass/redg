package de.yamass.redg.schema.model;

public interface DataType {
	String getName();
	String getJavaSqlTypeName();
	String getLocalTypeName();
	String getVendor();
	Integer getVendorTypeNumber();
	DataType getBaseType();
	boolean isAutoIncrementable();
	boolean isEnumerated();
}
