package com.aidream.libthriftclient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import org.apache.thrift.protocol.TProtocol;

/**
 * 
 * 动态代理的 Thrift Client
 * 
 * @author HouKangxi
 * 
 * @param <INTERFACE>
 */
public class ProxyClient<INTERFACE> implements InvocationHandler {
	private final INTERFACE iface;
	private final CommonClient client;

	public ProxyClient(TProtocol iprot, TProtocol oprot) {
		iface = createInterface();
		client = new CommonClient(iprot, oprot);
	}

	public ProxyClient(TProtocol prot) {
		iface = createInterface();
		client = new CommonClient(prot);
	}

	protected INTERFACE getIface() {
		return iface;
	}

	@SuppressWarnings("unchecked")
	private INTERFACE createInterface() {
		Type genType = getClass().getGenericSuperclass();
		Type itype = ((ParameterizedType) genType).getActualTypeArguments()[0];
		return (INTERFACE) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] { (Class<INTERFACE>) itype }, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		return client.execute(method, args);
	}
}