/**
 * 
 */
package com.openlibin.redis.core.reply.impl;

import com.openlibin.net.buffer.PooledBufferPool;
import com.openlibin.net.core.IoBuffer;
import com.openlibin.redis.core.enums.Type;
import com.openlibin.redis.util.ProtoUtils;

import io.netty.buffer.ByteBuf;  
 
/** 
 * 
 * @author liubing
 *
 */
public class BulkRedisReply extends CommonRedisReply {

	private int length;

	public BulkRedisReply(byte[] value) {
		this();
		this.value = value;
		if(value !=  null){
			this.length = value.length;
		}else{
			this.length = -1;
		}
	}

	public BulkRedisReply() {
		super(Type.BULK);
	}

	public void setLength(int length) {
		this.length = length;
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
		out.writeBytes(ProtoUtils.convertIntToByteArray(length));
		writeCRLF(out);   
		if (length > -1 && value != null) {
			out.writeBytes(value);
			writeCRLF(out);
		}
	}
	
	public byte[] getValue(){
		return value;
	}

	@Override
	public IoBuffer encodeBuf() {
		IoBuffer bb =  PooledBufferPool.DEFAULT.getBuffer();///  IoBuffer.allocate(1024);
		
		bb.writeByte(getType().getCode());

		bb.writeBytes(ProtoUtils.convertIntToByteArray(length));
		writeCRLF(bb);   
		if (length > -1 && value != null) {
			bb.writeBytes(value);
			writeCRLF(bb);
		}
		   
		return bb;
	}

}
