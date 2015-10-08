/**
 * 
 */
package com.aidream.libthriftclient;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 解析结果（结构体类和接口方法）的缓存
 * 
 * @author HouKangxi
 *
 */
public class Cache {
	public static interface IDataCreater<K, V> {
		V create(K k, Object... args);
	}

	@SuppressWarnings("rawtypes")
	private Map structFieldInfoMap = new HashMap();
	@SuppressWarnings("rawtypes")
	private Map ifaceMethodMap = new HashMap();

	private static Cache instance = new Cache();

	private Cache() {
	}

	public static Cache getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <K, V> V getStructCache(K modelClass, IDataCreater<K, V> c,
			Object... args) {
		synchronized (structFieldInfoMap) {
			V rs = (V) structFieldInfoMap.get(modelClass);
			if (rs == null) {
				rs = c.create(modelClass, args);
				structFieldInfoMap.put(modelClass, rs);
			}
			return rs;
		}
	}

	@SuppressWarnings("unchecked")
	public <K, V> V getIfaceMethodCache(K m, IDataCreater<K, V> c,
			Object... args) {
		synchronized (ifaceMethodMap) {
			V rs = (V) ifaceMethodMap.get(m);
			if (rs == null) {
				rs = c.create(m, args);
				ifaceMethodMap.put(m, rs);
			}
			return rs;
		}
	}
    @SuppressWarnings("unchecked")
	public void saveIfaceMethodCache(Method m,Object[]cache){
    	synchronized (ifaceMethodMap) {
    		ifaceMethodMap.put(m, cache);
    	}
    }
}
