/**
 * 
 */
package com.aidream.libthriftclient;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

/**
 * @author HouKangxi
 * @date 2014年10月29日
 */
public interface ProtocolHandler {

	public void read(TProtocol protocol, FieldInfo field, IStruct struct) throws TException;

	public void write(TProtocol protocol, FieldInfo field, IStruct struct) throws TException;

}
