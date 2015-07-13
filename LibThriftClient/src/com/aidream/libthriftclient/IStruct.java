/**
 * 
 */
package com.aidream.libthriftclient;

import org.apache.thrift.TFieldIdEnum;

/**
 * @author HouKangxi
 *
 */
public interface IStruct {

	public void setFieldValue(TFieldIdEnum field, Object fieldValue);

	public Object getFieldValue(TFieldIdEnum field);

}
