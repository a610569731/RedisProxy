/**
 * 
 */
package com.openlibin.redis.core.connection.impl;

import com.openlibin.redis.core.connection.IConnectionCallBack;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.impl.BulkRedisReply;
import com.openlibin.redis.core.reply.impl.MultyBulkRedisReply;
import com.openlibin.redis.core.reply.impl.StatusRedisReply;

import io.netty.channel.Channel;


/**
 * 
 * @author liubing
 *
 */
public class RedisConnectionCallBack implements IConnectionCallBack {
	
	private Channel channel;
	/* (non-Javadoc)
	 * @see com.wanda.ffan.redis.proxy.core.connection.IConnectionCallBack#handleReply(com.wanda.ffan.redis.proxy.core.reply.IRedisReply)
	 */
	@Override
	public void handleReply(IRedisReply reply) {
		System.out.println("接受到 "+reply);  
		if(reply instanceof BulkRedisReply){
 		}
		else if(reply instanceof StatusRedisReply){
			StatusRedisReply b = (StatusRedisReply) reply;
		}
		
		if(reply instanceof MultyBulkRedisReply){
			MultyBulkRedisReply b = (MultyBulkRedisReply) reply;
			System.out.println("b "+b);
		}
		
	    channel.writeAndFlush(reply);
	}
	/**
	 * @param channel
	 */
	public RedisConnectionCallBack(Channel channel) {
		super();
		this.channel = channel;
	}
	
	
}
