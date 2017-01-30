/**
 * 
 */
package com.openlibin.redis.core.connection;

import com.openlibin.redis.core.reply.IRedisReply;

/**
 * redis 连接回答回调方法
 * @author liubing
 *
 */
public interface IConnectionCallBack {
	
	/**
	 * 处理回答
	 * @param reply
	 */
	void handleReply(IRedisReply reply);
}
