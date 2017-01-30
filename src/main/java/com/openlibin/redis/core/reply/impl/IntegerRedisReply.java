/**
 * 
 */
package com.openlibin.redis.core.reply.impl;

  
import com.openlibin.net.buffer.PooledBufferPool;
import com.openlibin.net.core.IoBuffer;
import com.openlibin.redis.core.enums.Type;

import io.netty.buffer.ByteBuf;

/**
 * @author liubing
 *
 */
public class IntegerRedisReply extends CommonRedisReply {

	public IntegerRedisReply() {
		super(Type.INTEGER);
	}
   
	public IntegerRedisReply(byte[] value) {
		this();
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wanda.ffan.redis.proxy.core.reply.impl.AbstractRedisReply#doEncode
	 * (io.netty.buffer.ByteBuf)
	 */
	@Override
	public void doEncode(ByteBuf out) {
		out.writeBytes(value);
		writeCRLF(out); 
	}
	@Override
	public IoBuffer encodeBuf() {
		IoBuffer bb =  PooledBufferPool.DEFAULT.getBuffer();///  IoBuffer.allocate(1024);
		
		bb.writeByte(getType().getCode());

		bb.writeBytes(value); 
		writeCRLF(bb);

		return bb;
	}

}
