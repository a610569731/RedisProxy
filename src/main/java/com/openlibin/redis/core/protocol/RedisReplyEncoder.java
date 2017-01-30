/**
 * 
 */
package com.openlibin.redis.core.protocol;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.openlibin.redis.core.enums.ReplyState;
import com.openlibin.redis.core.enums.Type;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.RedisConstants;
import com.openlibin.redis.core.reply.impl.*;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author liubing
 *
 */
public class RedisReplyEncoder extends MessageToByteEncoder<IRedisReply> {

	@Override
	protected void encode(ChannelHandlerContext ctx, IRedisReply msg,
			ByteBuf out) throws Exception {
    	msg.encode(out);
	}

}
