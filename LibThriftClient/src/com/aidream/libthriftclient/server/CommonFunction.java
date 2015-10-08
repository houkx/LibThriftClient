/**
 * 
 */
package com.aidream.libthriftclient.server;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.thrift.ProcessFunction;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TStruct;

import com.aidream.libthriftclient.Cache;
import com.aidream.libthriftclient.CommonClient;
import com.aidream.libthriftclient.CommonStruct;
import com.aidream.libthriftclient.FieldInfo;
import com.aidream.libthriftclient.Log;

/**
 * @author HouKangxi
 *
 */
class CommonFunction extends ProcessFunction<Object, CommonStruct> {
	private FieldInfo[] returns;
	private FieldInfo[] arguments;
	private Method ifaceMethod;
	
	public CommonFunction(String methodName,Method ifaceMethod) {
		super(methodName);
		Object[] caches = Cache.getInstance().getIfaceMethodCache(
				ifaceMethod, new CommonClient(null));
		returns = (FieldInfo[]) caches[0];
		arguments = (FieldInfo[]) caches[1];
		this.ifaceMethod = ifaceMethod;
	}

	@Override
	public CommonStruct getEmptyArgsInstance() {
		TStruct struct = new TStruct(getMethodName()+"_args");
		CommonStruct argStruct = new CommonStruct(arguments, struct);
		return argStruct;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public TBase getResult(Object iface, CommonStruct arg) throws TException {
		TStruct struct = new TStruct(getMethodName()+"_result");
		CommonStruct rsStruct = new CommonStruct(returns, struct);
		Object rs = null;
		boolean hasReturn = false;
		if(returns != null && returns.length > 0){
			if (returns.length == 1) {
				if("success".equals(returns[0].getFieldName())){
					hasReturn = true;
				}
			}else{
				hasReturn = true;
			}
		}
		Log.d(getClass().getSimpleName(),"getResult:: hasReturn? "+hasReturn
				+ ", method="+getMethodName()+", ifaceMethod="+ifaceMethod+", returns = "+Arrays.toString(returns));
		try { 
			Object[] ps = arg.getFieldValues();
			rs = ifaceMethod.invoke(iface, ps);
		}
		catch (Throwable e) {
			Throwable cause = e.getCause();
			if(cause instanceof TException){
				Class exClass = cause.getClass();
				String thriftBasePkg = TException.class.getPackage().getName();
				String exPkg = exClass.getPackage().getName();
				if(exPkg.startsWith(thriftBasePkg)){
					// thrift 包内异常
					throw (TException)cause;
				}else{
					// 用户自定义异常
					short exIndex = hasReturn?(short)1:(short)0;
					TFieldIdEnum fieldEx = rsStruct.fieldForId(exIndex);
					rsStruct.setFieldValue(fieldEx, cause);
				}
			}else{
				e.printStackTrace();
			}
		}
		if (hasReturn) {
			rsStruct.setFieldValue(rsStruct.fieldForId(0), rs);
		}
		return rsStruct;
	}

	@Override
	protected boolean isOneway() {
		return false;
	}

}
