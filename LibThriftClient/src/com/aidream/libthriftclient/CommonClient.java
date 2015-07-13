/**
 * 
 */
package com.aidream.libthriftclient;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;

import com.aidream.libthriftclient.annotaion.ParameterName;

/**
 * Thrift 通用客户端
 * 
 * @author HouKangxi
 * @date 2014年10月24日
 */
public class CommonClient extends TServiceClient implements
		Cache.IDataCreater<Method, Object[]> {

	public CommonClient(TProtocol prot) {
		super(prot);
	}

	public CommonClient(TProtocol iprot, TProtocol oprot) {
		super(iprot, oprot);
	}

	/**
	 * 执行指定的接口方法
	 * <p>
	 * 注意：使用此方法时要求方法的每个参数都含有注解 @ParameterName
	 * 
	 * @param interfaceMethod
	 *            - 接口方法
	 * @param args
	 *            - 方法参数值
	 * @return
	 * @throws Exception
	 */
	public Object execute(Method interfaceMethod, Object[] args)
			throws Exception {
		Object[] caches = Cache.getInstance().getIfaceMethodCache(
				interfaceMethod, this);
		FieldInfo[] returns = (FieldInfo[]) caches[0];
		FieldInfo[] arguments = (FieldInfo[]) caches[1];
		return execute(interfaceMethod.getName(), returns, arguments, args);
	}

	/**
	 * 执行指定的接口方法
	 * 
	 * @param interfaceMethod
	 *            - 接口方法
	 * @param args
	 *            - 方法的参数名->参数值
	 * @return
	 * @throws Exception
	 */
	public Object execute(Method interfaceMethod,
			LinkedHashMap<String, Object> args) throws Exception {
		Object[] caches = Cache.getInstance().getIfaceMethodCache(
				interfaceMethod, this, args.keySet());
		FieldInfo[] returns = (FieldInfo[]) caches[0];
		FieldInfo[] arguments = (FieldInfo[]) caches[1];

		return execute(interfaceMethod.getName(), returns, arguments, args
				.values().toArray());
	}

	/**
	 * 执行指定的接口方法
	 * 
	 * @param methodName
	 *            - 方法名
	 * @param returns
	 *            - 返回类型
	 * @param arguments
	 *            - 参数
	 * @return
	 * @throws TException
	 */
	public Object execute(String methodName, FieldInfo[] returns,
			FieldInfo[] arguments, Object[] values) throws TException {
		send(methodName, arguments, values);
		return recv(methodName, returns);
	}

	private void send(String methodName, FieldInfo[] argInfos,
			Object[] argValues) throws TException {
		CommonStruct arguments = new CommonStruct(argInfos, new TStruct(
				methodName + "_args"));
		arguments.setFieldValues(argValues);
		sendBase(methodName, arguments);
	}

	private Object recv(String methodName, FieldInfo[] returns)
			throws TException {
		CommonStruct result = new CommonStruct(returns, new TStruct(methodName
				+ "_result"));
		receiveBase(result, methodName);
		switch (returns.length) {
		case 1: {
			Object rs = result.getFieldValue(returns[0]);
			procExceptionResult(rs);
			return null;
		}
		case 2: {
			Object rs = result.getFieldValue(returns[0]);
			Object ex = result.getFieldValue(returns[1]);
			procExceptionResult(ex);
			return rs;
		}
		}
		throw new TApplicationException(5, methodName
				+ " failed: unknown result");
	}

	private void procExceptionResult(Object rs) throws TException {
		if (rs != null) {
			if (rs instanceof TException) {
				throw (TException) rs;
			}
			if (rs instanceof Exception) {
				throw new TException((Exception) rs);
			}
		}
	}

	public FieldInfo[] parseArgurments(Method interfaceMethod,
			Iterable<String> argNames) {
		//
		Type[] paramTypes = interfaceMethod.getGenericParameterTypes();
		FieldInfo[] rs = null;
		if (paramTypes != null && paramTypes.length > 0) {
			rs = new FieldInfo[paramTypes.length];
			short i = 0;
			Iterator<String> itr = argNames.iterator();
			for (Type pt : paramTypes) {
				String argName = itr.next();
				FieldInfo cf = FieldInfo.parseFieldForType(pt, argName, ++i);
				rs[i - 1] = cf;
			}
		} else {
			rs = new FieldInfo[0];
		}
		return rs;
	}

	public FieldInfo[] parseReturns(Method interfaceMethod) {
		FieldInfo[] returns;
		Type returnType = interfaceMethod.getGenericReturnType();
		FieldInfo ex = new FieldInfo("ex", Types.Struct, (short) 1)
				.setFieldClass(interfaceMethod.getExceptionTypes()[0]);
		if (returnType == java.lang.Void.TYPE) {
			returns = new FieldInfo[1];
			returns[0] = ex;
		} else {
			returns = new FieldInfo[2];
			returns[0] = FieldInfo.parseFieldForType(returnType, "success",
					(short) 0);
			returns[1] = ex;
		}
		return returns;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] create(Method interfaceMethod, Object... args) {
		Iterable<String> argNames;
		if (args == null || args.length == 0) {
			Annotation[][] paramsAnns = interfaceMethod
					.getParameterAnnotations();
			String exceptionMsg = "Method %s parameter%s must add Annotation '@ParameterName'!";
			if (paramsAnns.length == 0) {
				throw new IllegalArgumentException(String.format(exceptionMsg,
						interfaceMethod.getName(),"s all"));
			}
			List<String> argNamesList = new ArrayList<String>(paramsAnns.length);
			for (Annotation[] anns : paramsAnns) {
				ParameterName paramNameAnn = null;
				for (Annotation ann : anns) {
					if (ann.annotationType() == ParameterName.class) {
						paramNameAnn = (ParameterName) ann;
						break;
					}
				}
				if (paramNameAnn == null) {
					throw new IllegalArgumentException(String.format(
							exceptionMsg, interfaceMethod.getName(),"[ " + argNamesList.size() + " ]"));
				}
				argNamesList.add(paramNameAnn.value());
			}
			argNames = argNamesList;
		} else {
			argNames = (Iterable<String>) args[0];
		}
		// 解析返回值
		FieldInfo[] returns = parseReturns(interfaceMethod);
		// 解析方法参数
		FieldInfo[] arguments = parseArgurments(interfaceMethod, argNames);
		return new Object[] { returns, arguments };
	}

}
