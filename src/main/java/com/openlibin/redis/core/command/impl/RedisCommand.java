/**
 * 
 */
package com.openlibin.redis.core.command.impl;

import java.util.List;

import com.openlibin.net.core.IoBuffer;
import com.openlibin.redis.core.command.IRedisCommand;
import com.openlibin.redis.core.reply.RedisConstants;
import com.openlibin.redis.util.ProtoUtils;

import io.netty.buffer.ByteBuf;


/**
 * @author liubing
 *
 */
public class RedisCommand implements IRedisCommand {

	private int argCount;
	private List<byte[]> args;
	
	private Long requestKey;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wanda.ffan.redis.proxy.core.command.IRedisCommand#encode(io.netty
	 * .buffer.ByteBuf)
	 */
	@Override
	public void encode(ByteBuf byteBuf) {
		byteBuf.writeByte((byte) RedisConstants.ASTERISK_BYTE);
	    byteBuf.writeBytes(ProtoUtils.convertIntToByteArray(args.size()));
	    writeCRLF(byteBuf);
	    for (byte[] arg : args) {
	      byteBuf.writeByte((byte) RedisConstants.DOLLAR_BYTE);
	      byteBuf.writeBytes(ProtoUtils.convertIntToByteArray(arg.length));
	      writeCRLF(byteBuf);
	      byteBuf.writeBytes(arg);
	      writeCRLF(byteBuf);
	    }
	}
	@Override
	public
	void encodeNew(IoBuffer byteBuf){
		byteBuf.writeByte((byte) RedisConstants.ASTERISK_BYTE);
	    byteBuf.writeBytes(ProtoUtils.convertIntToByteArray(args.size()));
	    writeCRLF(byteBuf); 
	    for (byte[] arg : args) {
	      byteBuf.writeByte((byte) RedisConstants.DOLLAR_BYTE);
	      byteBuf.writeBytes(ProtoUtils.convertIntToByteArray(arg.length));
	      writeCRLF(byteBuf);
	      byteBuf.writeBytes(arg);
	      writeCRLF(byteBuf);
	    }
	}
	/**
	 * @return the argCount
	 */
	public int getArgCount() {
		return argCount;
	}

	/**
	 * @param argCount
	 *            the argCount to set
	 */
	public void setArgCount(int argCount) {
		this.argCount = argCount;
	}
 
	/**
	 * @return the args
	 */
	public List<byte[]> getArgs() {
		return args;
	}

	/**
	 * @param args
	 *            the args to set
	 */
	public void setArgs(List<byte[]> args) {
		this.args = args;
	}

	
	private void writeCRLF(ByteBuf byteBuf) {
		byteBuf.writeByte(RedisConstants.CR_BYTE);
		byteBuf.writeByte(RedisConstants.LF_BYTE);
	}
	private void writeCRLF(IoBuffer byteBuf) {
		byteBuf.writeByte((byte)RedisConstants.CR_BYTE);
		byteBuf.writeByte((byte)RedisConstants.LF_BYTE);
	}

	/**
	 * @return the requestKey
	 */
	public Long getRequestKey() {
		return requestKey;
	}

	/**
	 * @param requestKey the requestKey to set
	 */
	public void setRequestKey(Long requestKey) {
		this.requestKey = requestKey;
	}
	
	public String getArgString(){
		if(args != null){
			String str = "";
			for(byte[] b: args){
				str+= new String(b);
			}
			return str;
		}
		return "";
		
	}
	
}
