/**
 * 
 */
package com.aidream.libthriftclient;

import org.apache.thrift.TFieldIdEnum;

/**
 * @author HouKangxi
 *
 */
class TmpStruct implements IStruct {
	private Object fieldValue;

	public TmpStruct() {
	}

	public TmpStruct(Object value) {
		this.fieldValue = value;
	}

	@Override
	public void setFieldValue(TFieldIdEnum arg0, Object value) {
		this.fieldValue = value;
	}

	@Override
	public Object getFieldValue(TFieldIdEnum arg0) {
		return fieldValue;
	}

}
