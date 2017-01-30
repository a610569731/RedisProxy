/**
 * 
 */
package com.openlibin.redis.core.protocol;

import java.util.ArrayList;
import java.util.List;

import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.command.impl.ShutdownCommand;
import com.openlibin.redis.core.enums.ReplyState;
import com.openlibin.redis.core.enums.RequestState;
import com.openlibin.redis.core.enums.Type;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.RedisConstants;
import com.openlibin.redis.core.reply.impl.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * 
 * request 解码
 * @author liubing
 *
 */
public class RedisRequestDecoder extends ReplayingDecoder<RequestState> {

	private RedisCommand requestCommand;

	public RedisRequestDecoder() {
		super(RequestState.READ_SKIP);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer,
			List<Object> out) throws Exception {
		
		switch (state()) {
	      case READ_SKIP: {
	        try {
	          skipChar(buffer);
	          checkpoint(RequestState.READ_INIT);
	        } finally {
	          checkpoint();
	        }
	      }
	      case READ_INIT: {	    	  
	        char ch = (char) buffer.readByte();
	        if (ch == RedisConstants.ASTERISK_BYTE) {//redis 协议开头
	          ch = (char) buffer.readByte();
	          if (ch == RedisConstants.CR_BYTE) {//shutdown命令
	            buffer.readByte();
	            checkpoint(RequestState.READ_SKIP);
	            out.add(new ShutdownCommand());
	          } else {
	            buffer.readerIndex(buffer.readerIndex() - 1);
	            requestCommand = new RedisCommand();
	            checkpoint(RequestState.READ_ARGC);
	          }
	        }else{
	        	throw new Exception("READ_INIT Unexpected character,ch:"+String.valueOf(ch));
	        }
	      }
	      case READ_ARGC: {
	    	if(requestCommand!=null){
	    		requestCommand.setArgCount(readInt(buffer));
	 	        checkpoint(RequestState.READ_ARG);
	    	}
	      }
	      case READ_ARG: {
	        List<byte[]> args = new ArrayList<>(requestCommand.getArgCount());
	        while (args.size() < requestCommand.getArgCount()) {
	        	char ch = (char) buffer.readByte();
	        	if (ch == '$') {
	   	          	int length = readInt(buffer);
	   	          	byte[] argByte = new byte[length];
	   	          	buffer.readBytes(argByte);
	   	          	buffer.skipBytes(2);//skip \r\n
	   	          	//LoggerUtils.info("String:"+new String(argByte));
	   	          	args.add(argByte);
	        	}else{
	        		throw new Exception("READ_ARG Unexpected character,ch:"+String.valueOf(ch));
	        	}
	        }
	        requestCommand.setArgs(args);
	        checkpoint(RequestState.READ_END);
	      }
	      case READ_END: {
	        RedisCommand command = this.requestCommand;
	        this.requestCommand = null;
	        checkpoint(RequestState.READ_INIT);
	        out.add(command);
	        return;
	      }
	      default:
	        throw new Error("can't reach here!");
	    }
	}

	private int readInt(ByteBuf buffer) throws Exception{
		StringBuilder sb = new StringBuilder();
		char ch = (char) buffer.readByte();
		while (ch != RedisConstants.CR_BYTE) {
			sb.append(ch);
			ch = (char) buffer.readByte();//\r读取
		}
		buffer.readByte();//\n读取
		
		try{
			int result= Integer.parseInt(sb.toString());
			return result;
		}catch(Exception e){//网络闭包引起
			throw new Exception("readInt Unexpected character,result:"+sb.toString()+",ch:"+String.valueOf(ch));
		}
	}

	private void skipChar(ByteBuf buffer) {
		for (;;) {
			char ch = (char) buffer.readByte();
			if (ch == RedisConstants.ASTERISK_BYTE) {
				buffer.readerIndex(buffer.readerIndex() - 1);
				break;
			}
		}
	}
	
}
