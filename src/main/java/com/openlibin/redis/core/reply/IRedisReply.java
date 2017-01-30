package com.openlibin.redis.core.reply;

import com.openlibin.net.core.IoBuffer;
import com.openlibin.redis.core.enums.Type;

import io.netty.buffer.ByteBuf;


/**
 * redis 回答
 * 
 * @author liubing
 *
 */
public interface IRedisReply {
	  
	  /**
	   * 获取类型
	   * @return
	   */
	  Type getType();
	  
	  /**
	   * 设置类型
	   * @param type
	   */
	  void setType(Type type);
	  
	  /**
	   * 编码
	   * @param out
	   */
	  void encode(ByteBuf out);
	  
	  
	  IoBuffer encodeBuf();
}
