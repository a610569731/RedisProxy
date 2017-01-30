/**
 * 
 */
package com.openlibin.redis.core.command;

import com.openlibin.net.core.IoBuffer;

import io.netty.buffer.ByteBuf;

/**
 * @author liubing
 *
 */
public interface IRedisCommand {
	/**
	 * 编码
	 * @param byteBuf
	 */
	void encode(ByteBuf byteBuf);
	void encodeNew(IoBuffer byteBuf);
 
}
