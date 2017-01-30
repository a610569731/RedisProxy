/**
 * 
 */
package com.openlibin.redis.core.server;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openlibin.redis.core.client.LBRedisClient;
import com.openlibin.redis.core.client.LBRedisClientPool;
import com.openlibin.redis.core.client.LBRedisClientPoolGroup;
import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.connection.impl.RedisConnectionCallBack;
import com.openlibin.redis.core.enums.RedisCommandEnums;
import com.openlibin.redis.core.reply.RedisConstants;
import com.openlibin.redis.core.reply.impl.ErrorRedisReply;
import com.openlibin.redis.core.reply.impl.StatusRedisReply;
import com.openlibin.redis.util.ProtoUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * redis服务端回答
 * 
 * @author liubing
 *
 */
public class LBRedisServerHandler extends SimpleChannelInboundHandler<RedisCommand> {

	  private Logger logger = LoggerFactory.getLogger(LBRedisServerHandler.class);
	  

	  
	  public LBRedisServerHandler(){
		  
	  }
	 public static  Map<LBRedisClient, Channel> bindChannel = new ConcurrentHashMap<>();
	  /**
	   * 接受请求
	   */
	  @Override
	  protected void channelRead0(ChannelHandlerContext ctx, RedisCommand request) throws Exception {
		  try{
	    Channel channel = ctx.channel();
	    RedisConnectionCallBack callback = new RedisConnectionCallBack(channel);

	    String command=new String(request.getArgs().get(0));
	    String key= "";
	    if(request.getArgs().size() >=2){
		     key= new String(request.getArgs().get(1));
	    }  
	    System.out.println("request command "+command +" "+key+ " "+request.getArgs().size());
	    if("AUTH".equals(command)){
	    	if("123456".equals(key)){
	    		System.err.println("服务器连接成功 ");
	    		StatusRedisReply b =  new StatusRedisReply("OK".getBytes());
	    		ctx.channel().writeAndFlush(b);
	    		return ; 
	    	}else{
	    		System.err.println("password error");
	    		ErrorRedisReply b =  new ErrorRedisReply("PASSWORD ERROR".getBytes());
	    		ctx.channel().writeAndFlush(b);
	    		ctx.channel().close();
	    	}
    		return ;
	    }
	    
	    if(request!=null&&request.getArgs().size()>1 && !command.equals(RedisConstants.KEYS)){//第一个是命令，第二个是key
	    //	RedisCommandEnums commandEnums=getRedisCommandEnums(command);
	    	LBRedisClientPool	lBRedisClientPool = LBRedisClientPoolGroup.get(key);
	    	LBRedisClient lbRedisClient = lBRedisClientPool.pop();
	    	
    		lbRedisClient.write(request, callback);
    		bindChannel.put( lbRedisClient,ctx.channel() );
    		
    		//lBRedisClientPool.push(lbRedisClient);  
	    } else if(request!=null&&request.getArgs().size()==1){//info 级别
	    	if("PING".equals(command)){
	    		//PONG
				StatusRedisReply b =  new StatusRedisReply("PONG".getBytes());
	    		ctx.channel().writeAndFlush(b);
	    		return ;
	    	}else if("QUIT".equals(command)){
	    		//PONG
				StatusRedisReply b =  new StatusRedisReply("OK".getBytes());
	    		ctx.channel().writeAndFlush(b);
	    		return ;
	    	}
	    	
	    	//QUIT
	    	LBRedisClientPool	lBRedisClientPool = LBRedisClientPoolGroup.get("1");
	    	
	    	LBRedisClient lbRedisClient = lBRedisClientPool.pop();
    		lbRedisClient.write(request, callback);
    		lBRedisClientPool.push(lbRedisClient);  
    		
	    	/*
	    	 *	command PING  1
				command QUIT  1
	    	for(String key:ffanRedisServerBeanMap.keySet()){
	    		AbstractPoolClient ffanRedisClient=ffanRedisServerBeanMap.get(key);
	    		if(!ffanRedisClient.isAvailable()){
		    		ffanRedisClient.open();
		    	}
	    		ffanRedisClient.write(request,callback);
	    	}*/
	    }  
		  }catch(Exception ex){
			  ex.printStackTrace();
		  }
	  }
	    
	  /**
	   * 根据参数获取枚举类
	   * @param command
	   * @return
	   */
	  private RedisCommandEnums getRedisCommandEnums(String command){
		  try{
			 return  RedisCommandEnums.valueOf(command.toUpperCase());
		  }catch(Exception e){
			  return null;
		  }
	  }
	  
	  
	  @Override
	  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	    //logger.info(ctx.channel().remoteAddress().toString() + " had closed!!!");
	  }

	  @Override
	  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	    if (cause instanceof IOException){
	      String message = cause.getMessage();
	      if (message!=null && "远程主机强迫关闭了一个现有的连接。".equals(message)){
	        logger.warn("Client closed!");
	      }else {
	        logger.error("出错，客户端关闭连接");
	      }
	      ctx.channel().close();
	    }else {
	      logger.error("出错，关闭连接");
	      ctx.channel().write(new ErrorRedisReply(ProtoUtils.buildErrorReplyBytes("closed by upstream")));
	    }
	  }

}
