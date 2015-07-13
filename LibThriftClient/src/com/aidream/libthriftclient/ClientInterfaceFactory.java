/**
 * 
 */
package com.aidream.libthriftclient;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;

/**
 * 
 * @author HouKangxi
 * @date 2014年11月2日-下午12:27:14
 * 
 */
public class ClientInterfaceFactory {
	
	public static <INTERFACE> INTERFACE getClientInterface(
			Class<INTERFACE> ifaceClass, TProtocol iprot) {
		return newProxyInterface(ifaceClass, getClient(iprot));
	}

	public static <INTERFACE> INTERFACE getClientInterface(
			Class<INTERFACE> ifaceClass, TProtocol iprot, TProtocol oprot) {
		return newProxyInterface(ifaceClass, getClient(iprot, oprot));
	}

	@SuppressWarnings("unchecked")
	private static <INTERFACE> INTERFACE newProxyInterface(
		final Class<INTERFACE> ifaceClass, final CommonClient client) {
		// 检查是不是未缩减的接口jar包
		Map<Method, List<String>> map =null;
		try {
			map = parseOrigIfaceMethodArgNames(ifaceClass);
		} catch (Exception e) {
		}
		if (map != null) {
//			System.out.println("旧版的接口jar包");
			for (Map.Entry<Method, List<String>> entry : map.entrySet()) {
				Method m = entry.getKey();
				List<String> argNames = entry.getValue();
				FieldInfo[] args = client.parseArgurments(m, argNames);
				FieldInfo[] returns = client.parseReturns(m);
				Cache.getInstance().saveIfaceMethodCache(m,
						new Object[] { returns, args });
			}
		}else{
//			System.out.println("已经缩减的接口jar包");
		}
		InvocationHandler handler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				if(method.getName().equals("toString")){
					return ifaceClass.getName();
				}
				return client.execute(method, args);
			}
		};
		return (INTERFACE) Proxy.newProxyInstance(ifaceClass.getClassLoader(),
				new Class[] { ifaceClass }, handler);
	}

	private static CommonClient getClient(TProtocol iprot, TProtocol oprot) {
		CommonClient client = new CommonClient(iprot, oprot);
		return client;
	}

	private static CommonClient getClient(TProtocol iprot) {
		CommonClient client = new CommonClient(iprot);
		return client;
	}
	
	/**
	 * (对于未缩减的接口jar包)解析所有接口方法的参数名
	 * <p>
	 * 通过与接口同级的Service内部类来解析
	 * 
	 * @param ifaceClass
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 */
	private static Map<Method, List<String>> parseOrigIfaceMethodArgNames(
			Class<?> ifaceClass) throws NoSuchMethodException,
			SecurityException, NoSuchFieldException {
		Class<?> outerClass = ifaceClass.getEnclosingClass();
		// 没有外部Service类，肯定不是原始接口包
		if (outerClass == null) {
			return null;
		}
		Class<?>[] innerClasses = outerClass.getClasses();
		if (innerClasses == null || innerClasses.length < 2) {
			// 如果接口下没有同级别的Service内部类，则不处理
			return null;
		}
		Map<Method, List<String>> map = new HashMap<Method, List<String>>();
		Comparator<TField> c = new Comparator<TField>() {
			@Override
			public int compare(TField o1, TField o2) {
				return o1.id - o2.id;
			}
		};
		for (Class<?> cls : innerClasses) {
			if (!(TBase.class.isAssignableFrom(cls))) {
				continue;
			}
			String name = null;
			try {
				Field descField = cls.getDeclaredField("STRUCT_DESC");
				descField.setAccessible(true);
				TStruct struct = (TStruct) descField.get(null);
				name = struct.name;
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (name == null || name.endsWith("_result")) {
				continue;
			}
			int _i = name.lastIndexOf('_');
			String methodName = name.substring(0, _i);
			List<TField> tfs = new ArrayList<TField>();
			Field[] fs = cls.getDeclaredFields();
			try {
				for (Field f : fs) {
					if (f.getType() == TField.class) {
						f.setAccessible(true);
						tfs.add((TField) f.get(null));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Collections.sort(tfs, c);
			Class<?>[] parameterTypes = new Class<?>[tfs.size()];
			List<String> argNames = new ArrayList<String>(parameterTypes.length);
			for (int i = 0, len = tfs.size(); i < len; i++) {
				String fieldName = tfs.get(i).name;
				argNames.add(fieldName);
				parameterTypes[i] = cls.getField(fieldName).getType();
			}
			Method ifaceMethod = ifaceClass.getMethod(methodName,
					parameterTypes);
			map.put(ifaceMethod, argNames);
		}
		return map;
	}
}
