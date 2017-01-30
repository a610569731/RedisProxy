/**
 * 
 */
package com.openlibin.redis.core.client;


import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.connection.IConnectionCallBack;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

/**
 * 解码处理
 * 
 * @author liubing
 *
 */
public class LBRedisClientOutHandler extends ChannelOutboundHandlerAdapter {
	
	//public static final AttributeKey<IConnectionCallBack> CALLBACK_KEY = AttributeKey.valueOf("CALLBACK_KEY");
	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		if(msg instanceof RedisCommand){
				ctx.write(msg, promise);
		}else{
		//	LoggerUtils.error("write redis server msg not instanceof RedisCommand");
		}
		
		
	}
}
