/**
 * 
 */
package com.openlibin.redis.core.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.connection.IConnectionCallBack;
import com.openlibin.redis.core.enums.Type;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.RedisConstants;
import com.openlibin.redis.core.reply.impl.BulkRedisReply;
import com.openlibin.redis.core.reply.impl.MultyBulkRedisReply;
import com.openlibin.redis.core.reply.impl.StatusRedisReply;
import com.openlibin.redis.core.server.LBRedisServerHandler;
import com.openlibin.redis.util.ProtoUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 目标服务器与客户端通道写入
 * @author liubing
 *
 */
public class LBRedisClientInHandler extends SimpleChannelInboundHandler<IRedisReply> {
	private String password;
	
	LBRedisClient lbRedisClient = null;
	public LBRedisClientInHandler(LBRedisClient lbRedisClient){
		this.lbRedisClient = lbRedisClient;
	}
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public static Map<Long, IConnectionCallBack> callBackMap=new ConcurrentHashMap<Long, IConnectionCallBack>();//key requestKeyId  IConnectionCallBack
	
	public static Map<Long, Long> requestThreadMap=new ConcurrentHashMap<Long, Long>();
 
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
 		//有新的客户端连接
		String passwrod = getPassword();
		if(passwrod != null && !"".equals(passwrod)){
			RedisCommand msg = new RedisCommand();
			List<byte[]> list = new ArrayList<>();
			System.out.println("开始连接 "+passwrod);
			list.add("AUTH".getBytes());
			list.add(passwrod.getBytes());
			msg.setArgs(list);
			msg.setArgCount(2); 
			ctx.writeAndFlush(msg);
		}
	//	logger.debug("channelActive: " + ctx.channel().remoteAddress());
	}
	boolean isFirst = false;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IRedisReply msg)
			throws Exception {
			Type type = msg.getType();
			if(!isFirst&& type == Type.STATUS){
				StatusRedisReply b = (StatusRedisReply) msg;
				isFirst = true;
				return ;
			}     
			//Long requestKey=requestThreadMap.get(Thread.currentThread().getId());
			//System.out.println("requestKey "+requestKey);
			//IConnectionCallBack callBack = callBackMap.get(requestKey);  
			if(LBRedisServerHandler.bindChannel.containsKey(lbRedisClient)){
				Channel channel = LBRedisServerHandler.bindChannel.get(lbRedisClient);
				System.out.println("发送 "+msg);
				if(msg instanceof MultyBulkRedisReply){
					MultyBulkRedisReply b = (MultyBulkRedisReply) msg;
					List<IRedisReply> list = b.getList();
					System.out.println(list.size());

					for(IRedisReply iRedisReply:list){
						if(iRedisReply instanceof BulkRedisReply ){
							BulkRedisReply bulkRedisReply = (BulkRedisReply)iRedisReply;
							if(bulkRedisReply.getValue() != null){
								System.out.println(new String(bulkRedisReply.getValue()));
							}else{
								System.out.println(new String("null"));
							}
						}
					}
					
				}
				channel.writeAndFlush(msg); 
				 LBRedisServerHandler.bindChannel.remove(lbRedisClient);
				 
				lbRedisClient.getLbRedisClientPool().push(lbRedisClient);
			}
			//LBRedisServerHandler.bindChannel.get(ctx.channel());
			
			//IConnectionCallBack callBack = ctx.channel().attr(LBRedisClientOutHandler.CALLBACK_KEY).get();
			/*if(callBack!=null){ 
				callBack.handleReply(msg);
			}else{  
				//LoggerUtils.error(ctx.channel().remoteAddress().toString()+" has no callBack");
			}*/
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		
		super.exceptionCaught(ctx, cause);
	}
}
