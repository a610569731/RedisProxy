/**
 * 
 */
package com.openlibin.redis.core.protocol;


import com.openlibin.redis.core.command.impl.RedisCommand;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * request编码
 * @author liubing
 *
 */
public class RedisRequestEncoder extends MessageToByteEncoder<RedisCommand> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RedisCommand msg,
			ByteBuf out) throws Exception {
    	msg.encode(out);
	}

}
