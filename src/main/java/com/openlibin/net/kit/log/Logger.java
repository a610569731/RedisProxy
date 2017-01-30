/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.openlibin.net.kit.log;

import com.openlibin.net.kit.log.impl.JdkLoggerFactory;


public abstract class Logger { 
	private static LoggerFactory factory; 
	
	static {
		initDefaultFactory();
	}
	
	public static void setLoggerFactory(LoggerFactory factory) {
		if (factory != null) {
			Logger.factory = factory;
		}
	}
	
	public static Logger getLogger(Class<?> clazz) {
		return factory.getLogger(clazz);
	}
	
	public static Logger getLogger(String name) {
		return factory.getLogger(name);
	}
	
	public static void initDefaultFactory() {
		if (factory != null){
			return ;
		}
		
		try {
			//default to Log4j
			Class.forName("org.apache.log4j.Logger");
			String defaultFactory = String.format("%s.impl.Log4jLoggerFactory", Logger.class.getPackage().getName());
			Class<?> factoryClass = Class.forName(defaultFactory);
			factory = (LoggerFactory)factoryClass.newInstance();
			return;
		} catch (Exception e) {  
		}
		
		try {
			//try slf4j
			Class.forName("org.slf4j.Logger");
			String defaultFactory = String.format("%s.impl.Sl4jLoggerFactory", Logger.class.getPackage().getName());
			Class<?> factoryClass = Class.forName(defaultFactory);
			factory = (LoggerFactory)factoryClass.newInstance();
			return;
		} catch (Exception e) { 
		} 
		
		if(factory == null){
			factory = new JdkLoggerFactory();
		}
	}
	
	public void debug(String format, Object... args){
		debug(String.format(format, args));
	} 
	
	public void info(String format, Object... args){
		info(String.format(format, args));
	}
	
	public void warn(String format, Object... args){
		warn(String.format(format, args));
	}
	
	public void error(String format, Object... args){
		error(String.format(format, args));
	}
	
	public abstract void debug(String message);
	
	public abstract void debug(String message, Throwable t);
	
	public abstract void info(String message);
	
	public abstract void info(String message, Throwable t);
	
	public abstract void warn(String message);
	
	public abstract void warn(String message, Throwable t);
	
	public abstract void error(String message);
	
	public abstract void error(String message, Throwable t);
	
	public abstract void fatal(String message);
	
	public abstract void fatal(String message, Throwable t);
	
	public abstract boolean isDebugEnabled();

	public abstract boolean isInfoEnabled();

	public abstract boolean isWarnEnabled();

	public abstract boolean isErrorEnabled();
	
	public abstract boolean isFatalEnabled();
}

