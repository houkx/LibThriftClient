/**
 * 
 */
package org.slf4j;

/**
 * @author HouKangxi
 * @date 2014年11月3日
 */
public class LoggerFactory {

	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	public static Logger getLogger(String name) {
		LoggerImpl_empty logger = new LoggerImpl_empty();
		return logger;
	}
}
