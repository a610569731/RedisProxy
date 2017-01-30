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
package com.openlibin.net.kit.pool.impl;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.net.kit.pool.ObjectFactory;
import com.openlibin.net.kit.pool.Pool;
import com.openlibin.net.kit.pool.PoolConfig;
import com.openlibin.net.kit.pool.PoolFactory;

public class CommonsPool2Factory implements PoolFactory {
	private static final Logger log = Logger.getLogger(Pool.class);
	@Override
	public <T> Pool<T> getPool(ObjectFactory<T> factory, PoolConfig config) { 
		log.debug("Using Apache Commons-pool2");
		return new CommonsPool2<T>(factory, config);
	}
}

class CommonsPool2<T> extends Pool<T> {
	private GenericObjectPool<T> support; 
	
	public CommonsPool2(ObjectFactory<T> supportFactory, PoolConfig config) {   
		Commons2PoolFactory factory = new Commons2PoolFactory(supportFactory); 
		GenericObjectPoolConfig poolConfig = null;
		if(config.getSupport() instanceof GenericObjectPoolConfig){
			poolConfig = (GenericObjectPoolConfig)config.getSupport();
		} else {
			poolConfig = new GenericObjectPoolConfig();
			poolConfig.setMaxTotal(config.getMaxTotal());
			poolConfig.setMaxIdle(config.getMaxIdle());
			poolConfig.setMinIdle(config.getMinIdle());
			poolConfig.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
		}
		
		this.support = new GenericObjectPool<T>(factory, poolConfig);
	}

	@Override
	public T borrowObject() throws Exception { 
		return support.borrowObject();
	}

	@Override
	public void returnObject(T obj){ 
		support.returnObject(obj);
	}

	@Override
	public void close() { 
		support.close();
	}  
	
	private class Commons2PoolFactory extends BasePooledObjectFactory<T> {
		ObjectFactory<T> support; 
		
		public Commons2PoolFactory(ObjectFactory<T> support){ 
			this.support = support;
		}
		
		@Override
		public T create() throws Exception { 
			return support.createObject();
		}
		
		@Override
		public PooledObject<T> wrap(T obj) {
			return new DefaultPooledObject<T>(obj);
		}
		
		@Override
		public void destroyObject(PooledObject<T> p) throws Exception {
			T obj = p.getObject();
			support.destroyObject(obj);
		}
		
		@Override
		public boolean validateObject(PooledObject<T> p) {
			return support.validateObject(p.getObject());
		}
	}

	@Override
	public int getActiveCount() {
		// TODO Auto-generated method stub
		return this.support.getNumActive();
	}
}