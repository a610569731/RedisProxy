package com.openlibin.server.coder;
 


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.util.ArrayList;
import java.util.List;

import com.openlibin.net.core.Codec;
import com.openlibin.net.core.IoBuffer;
import com.openlibin.net.core.SelectorThread;
import com.openlibin.net.core.Session;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.enums.RequestState;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.RedisConstants;


public class RequestRedisCodec implements Codec{  
	private static final Logger log = Logger.getLogger(RequestRedisCodec.class);

	@Override
	public IoBuffer encode(Object obj) {  
		try{
			if(!(obj instanceof IRedisReply)){ 
				throw new RuntimeException("Message unknown"); 
			}  
			IRedisReply msg = (IRedisReply)obj;   
			IoBuffer buf = msg.encodeBuf(); 

			buf.flip();
			return buf; 
		}catch(Exception ex){
			ex.printStackTrace();
		}
	
		return null; 
	}
	
//	RedisCommand requestCommand = null;
//	RequestState requestState = RequestState.READ_INIT ;
	
	 
	
	public void checkpoint(Session session,RequestState requestState){
		session.requestState = requestState;
	}
 
	@Override
	public Object decode(IoBuffer buffer,Session sess)  {
		
		try{
	
			if(buffer.limit() < 1){
				return null ;
			}
			
			switch (sess.requestState) {
		      case READ_INIT: {	 
		    	  if(buffer.limit() < 3){
						return null ;
					}
		  	        char ch = (char) buffer.readByte();
		  	
		  	        if (ch == RedisConstants.ASTERISK_BYTE) {//redis 协议开头
		  		          if (ch == RedisConstants.CR_BYTE) {//shutdown命令
		  		        	  buffer.readByte();
		  		          } else {
		  		        	sess.requestCommand = new RedisCommand();
		  		        	 //  System.out.println( " init "+ch+"  "+ buffer.limit());
		  		        	   checkpoint(sess,RequestState.READ_ARGC);
		  		          }
		  	        }else{ 
		  	        	throw new Exception("READ_INIT Unexpected character,ch:"+String.valueOf(ch));
		  	        }
		      }
		      case READ_ARGC: {
			    	if(sess.requestCommand != null){
			    		int count = readInt(buffer,false); 
			    		if(count <= -2){
			    			return null;
			    		} 
			    	 	sess.requestCommand.setArgCount(count);
			 	        checkpoint(sess,RequestState.READ_ARG);
			    	}
			  } 
		     case READ_ARG: {
		         List<byte[]> args =  null;
		    	 if(sess.requestCommand.getArgs() != null){
		    		 args = sess.requestCommand.getArgs();
		    	 }else{
		    		 args = new ArrayList<>(sess.requestCommand.getArgCount());
		    		 sess.requestCommand.setArgs(args);
		    	 }
		    //	 System.out.println("READ_ARG" +requestCommand.getArgCount());

			        while (args.size() < sess.requestCommand.getArgCount()) {
				    	 buffer.mark();

			    	  	if(buffer.remaining()<1  ) {
			    			buffer.reset();
			    			log.info("rest");
			    			return null;
			    	  	} 
			        	char ch = (char) buffer.readByte();
			        	if (ch == '$') {
			   	          	int length = readInt(buffer,true); 
				    		if(length <= -2){
				    			return null;
				    		}
				    		if(buffer.remaining() < length+2  ) {
				    			buffer.reset();
				    			return null;
				    	  	}
			   	          	byte[] argByte = new byte[length];
			   	          	

			   	          	buffer.readBytes(argByte);
			   	          	buffer.skipBytes(2);//skip \r\n
			   	          	//LoggerUtils.info("String:"+new String(argByte));
			   	          	args.add(argByte);
			        	}else{
			        		throw new Exception("READ_ARG Unexpected character,ch:"+String.valueOf(ch));
			        	}
			        }
			        
			     //   requestCommand.setArgs(args);
			        if(sess.requestCommand.getArgs().size()== sess.requestCommand.getArgCount()){
			        	checkpoint(sess,RequestState.READ_END);
			        }
			  }
		     case READ_END: {
 
			        RedisCommand command = sess.requestCommand;
			        sess.requestCommand = null;
			        checkpoint(sess,RequestState.READ_INIT);
					return command;
			   }
			 default:
			        throw new Error("can't reach here!");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	} 
	 

	private int readInt(IoBuffer buffer,boolean isMark) throws Exception{
		if(!isMark){
			buffer.mark();
		}
		if(buffer.limit() < 1){
			log.info("reset");
			buffer.reset();
			return -2;
		}
		StringBuilder sb = new StringBuilder();
		
		byte b = buffer.readByte();
		 
		char ch = (char) b;
		while (ch != RedisConstants.CR_BYTE) {
			sb.append(ch);
			if(buffer.limit() < 1){
				buffer.reset();
				return -2;
			}
			ch = (char) buffer.readByte();//\r读取
		}
		if(buffer.limit() < 1){
			buffer.reset();
			return -2;
		}
		buffer.readByte();//\n读取
		
		try{
			int result= Integer.parseInt(sb.toString());
			return result;
		}catch(Exception e){//网络闭包引起
			e.printStackTrace();
			throw new Exception("readInt Unexpected character,result:"+sb.toString()+",ch:"+String.valueOf(ch));
		}
	}
	 
}