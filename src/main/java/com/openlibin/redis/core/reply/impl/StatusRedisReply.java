/**
 * 
 */
package com.openlibin.redis.core.reply.impl;


import com.openlibin.net.buffer.PooledBufferPool;
import com.openlibin.net.core.IoBuffer;
import com.openlibin.redis.core.enums.Type;

import io.netty.buffer.ByteBuf;

/**
 * redis server 状态回答
 * @author liubing
 *
 */
public class StatusRedisReply extends CommonRedisReply {

	public StatusRedisReply() {
		super(Type.STATUS);
	}

	public StatusRedisReply(byte[] value) {
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
		// TODO Auto-generated method stub
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
