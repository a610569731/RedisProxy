/**
 * 
 */
package com.openlibin.redis.core.reply.impl;

import java.util.ArrayList;
import java.util.List;

import com.openlibin.net.buffer.PooledBufferPool;
import com.openlibin.net.core.IoBuffer;
import com.openlibin.redis.core.enums.Type;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.RedisConstants;
import com.openlibin.redis.util.ProtoUtils;

import io.netty.buffer.ByteBuf;

/**
 * @author liubing
 *
 */
public class MultyBulkRedisReply extends CommonRedisReply {

	protected List<IRedisReply> list = new ArrayList<IRedisReply>();

	private int count;

	public void setCount(int count) {
		this.count = count;
	}

	public MultyBulkRedisReply() {
		super(Type.MULTYBULK);
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
		try{
		out.writeBytes(ProtoUtils.convertIntToByteArray(count));
		writeCRLF(out);
		for (IRedisReply reply : list) {
			if (reply instanceof IntegerRedisReply) {
				if (value == null&&count==0) {
					out.writeByte(RedisConstants.COLON_BYTE);
					out.writeBytes(ProtoUtils.convertIntToByteArray(-1));
					writeCRLF(out);
				} else {
					out.writeByte(RedisConstants.COLON_BYTE);
					out.writeBytes(ProtoUtils   
							.convertIntToByteArray(((IntegerRedisReply) reply).value.length));
					writeCRLF(out);
					out.writeBytes(((IntegerRedisReply) reply).value);
					writeCRLF(out);
				}

			} else if (reply instanceof BulkRedisReply) {
				if (value == null&&count==0) {
					out.writeByte(RedisConstants.DOLLAR_BYTE);
					out.writeBytes(ProtoUtils.convertIntToByteArray(-1));
					writeCRLF(out);
				} else {  
					out.writeByte(RedisConstants.DOLLAR_BYTE);
					if(((BulkRedisReply) reply).value == null){
						String value = "";
						out.writeBytes(ProtoUtils
								.convertIntToByteArray(value.length()));
						writeCRLF(out);  
						out.writeBytes(value.getBytes());
						writeCRLF(out);
					}else{
						out.writeBytes(ProtoUtils
								.convertIntToByteArray(((BulkRedisReply) reply).value.length));
						writeCRLF(out);
						out.writeBytes(((BulkRedisReply) reply).value);
						writeCRLF(out);
					}
				}

			}
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	@Override
	public IoBuffer encodeBuf() {
		IoBuffer bb =  PooledBufferPool.DEFAULT.getBuffer();///  IoBuffer.allocate(1024);
		
		bb.writeByte(getType().getCode());

		
		bb.writeBytes(ProtoUtils.convertIntToByteArray(count));
		writeCRLF(bb);
		for (IRedisReply reply : list) {
			if (reply instanceof IntegerRedisReply) {
				if (value == null&&count==0) {
					bb.writeByte((byte)RedisConstants.COLON_BYTE);
					bb.writeBytes(ProtoUtils.convertIntToByteArray(-1));
					writeCRLF(bb);
				} else {
					bb.writeByte((byte)RedisConstants.COLON_BYTE);
					bb.writeBytes(ProtoUtils   
							.convertIntToByteArray(((IntegerRedisReply) reply).value.length));
					writeCRLF(bb);
					bb.writeBytes(((IntegerRedisReply) reply).value);
					writeCRLF(bb);
				}

			} else if (reply instanceof BulkRedisReply) {
				if (value == null&&count==0) {
					bb.writeByte((byte)RedisConstants.DOLLAR_BYTE);
					bb.writeBytes(ProtoUtils.convertIntToByteArray(-1));
					writeCRLF(bb);
				} else {  
					bb.writeByte((byte)RedisConstants.DOLLAR_BYTE);
					if(((BulkRedisReply) reply).value == null){
						String value = "";
						bb.writeBytes(ProtoUtils
								.convertIntToByteArray(value.length()));
						writeCRLF(bb);  
						bb.writeBytes(value.getBytes());
						writeCRLF(bb);
					}else{
						bb.writeBytes(ProtoUtils
								.convertIntToByteArray(((BulkRedisReply) reply).value.length));
						writeCRLF(bb);
						bb.writeBytes(((BulkRedisReply) reply).value);
						writeCRLF(bb);
					}
				}

			}
		}

		return bb;
	}
	public  List<IRedisReply> getList(){
		return list;
	}
	public  void  clear(){
		count = 0;
		if(list != null){
			list.clear();
		}
	}
	public void addReply(IRedisReply reply) {
		list.add(reply);
	}

	public int getCount() {
		return count;
	}
	
}
