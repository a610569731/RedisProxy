/**
 * 
 */
package com.openlibin.redis.core.reply.impl;

import com.openlibin.redis.core.enums.Type;

/**
 * @author liubing
 *
 */
public abstract class CommonRedisReply extends AbstractRedisReply {

	protected byte[] value;

	public CommonRedisReply(Type type) {
		super(type);  
	}

	public void setValue(byte[] value) {
		this.value = value;
	}
	public byte[]  getValue() {
		return  value;
	} 

}
