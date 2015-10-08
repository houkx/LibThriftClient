/**
 * 
 */
package com.aidream.libthriftclient.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.ProcessFunction;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;

import com.aidream.libthriftclient.Log;

/**
 * 通用 Processor
 * <p>
 * 只要是 iface 的实现类中公开的方法都可以作为 thrift 接口方法对外提供
 * 
 * @author HouKangxi
 *
 */
public class CommonProcessor implements TProcessor {
	private final Object iface;
	private Map<String, Method> methodMap = new HashMap<String, Method>();
	@SuppressWarnings("rawtypes")
	private Map<String, ProcessFunction<Object, ? extends TBase>> origFuncMap;

	public CommonProcessor(Object iface) {
		this(null, iface);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CommonProcessor(Class<?> ifaceClass, Object iface) {
		this.iface = iface;
		if (ifaceClass != null) {
			// 旧版接口(即thrift自动生成的接口)的处理，利用其中的 methodName<-->ProcessFunction 映射
			// 当然，也可以全部用新的方式，此时 ifaceClass==null
			try {
				Class<?> outerClass = ifaceClass.getEnclosingClass();
				Class<?> procClass = Class.forName(outerClass
						.getCanonicalName() + "$Processor");
				Method m = procClass.getDeclaredMethod("getProcessMap",
						Map.class);
				m.setAccessible(true);
				origFuncMap = (Map<String, ProcessFunction<Object, ? extends TBase>>) m
						.invoke(null, new HashMap());
			} catch (Throwable e) {
			}
		}
		// map all public methods by methodName
		for (Method m : iface.getClass().getMethods()) {
			methodMap.put(m.getName(), m);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean process(TProtocol in, TProtocol out) throws TException {
		TMessage msg = in.readMessageBegin();
		ProcessFunction fn = getProcessFunction(msg.name);
		if (fn == null) {
			TProtocolUtil.skip(in, (byte) 12);
			in.readMessageEnd();
			TApplicationException x = new TApplicationException(1,
					"Invalid method name: '" + msg.name + "'");
			out.writeMessageBegin(new TMessage(msg.name, (byte) 3, msg.seqid));
			x.write(out);
			out.writeMessageEnd();
			out.getTransport().flush();
			return true;
		}
		fn.process(msg.seqid, in, out, iface);
		return true;
	}

	@SuppressWarnings({ "rawtypes" })
	private ProcessFunction getProcessFunction(final String name) {
		if (origFuncMap != null) {
			ProcessFunction origFunc = origFuncMap.get(name);
			if (origFunc != null) {
				Log.d(getClass().getSimpleName(), name+":使用旧 Function ");
				return origFunc;
			}
		}
		Log.d(getClass().getSimpleName(), name+":使用新的 Function ");
		Method ifaceMethod = methodMap.get(name);
		if (ifaceMethod == null) {
			return null;
		}
		//
		CommonFunction fun = new CommonFunction(name,
				ifaceMethod);
		return fun;
	}

}
