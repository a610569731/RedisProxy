/**
 * 
 */
package com.openlibin.redis.core.command.impl;

import com.openlibin.net.core.IoBuffer;
import com.openlibin.redis.core.command.IRedisCommand;

import io.netty.buffer.ByteBuf;


/**
 * @author liubing
 *
 */
public class ShutdownCommand implements IRedisCommand {

	/* (non-Javadoc)
	 * @see com.wanda.ffan.redis.proxy.core.command.IRedisCommand#encode(io.netty.buffer.ByteBuf)
	 */
	@Override
	public void encode(ByteBuf byteBuf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void encodeNew(IoBuffer byteBuf) {
		// TODO Auto-generated method stub
		 
		
	}

}
