package com.openlibin.client.pool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.openlibin.client.coder.ClientRedisCodec;
import com.openlibin.net.Client;
import com.openlibin.net.Server;
import com.openlibin.net.core.SelectorGroup;
import com.openlibin.net.core.SelectorThread;
import com.openlibin.net.core.Session;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.impl.BulkRedisReply;
import com.openlibin.redis.core.reply.impl.ErrorRedisReply;
import com.openlibin.redis.core.reply.impl.MultyBulkRedisReply;
import com.openlibin.redis.core.reply.impl.StatusRedisReply;
import com.openlibin.schedule.ScheduledExecutor;
import com.openlibin.server.RedisServer;
import com.openlibin.slice.Node;
import com.openlibin.util.StringUtil;
import com.openlibin.util.TimeUtil;
 
public class RedisClient  extends Client<RedisCommand,IRedisReply> {
	private Node node;
	private String id = UUID.randomUUID().toString();
	private static final Logger log = Logger.getLogger(RedisClient.class);
	private boolean isPassword = false;
	public RedisClient(Node node, SelectorGroup selectorGroup) {
		//Client
		super(node.getIp(),node.getPort(), selectorGroup);
		this.node = node;
		
		
		if(!StringUtil.isEmpty(node.getPassword())){
			isPassword = true;
		}else{
			isFisrt .set(true);
		} 
		
		
		this.codec(new ClientRedisCodec());
		 try {
			log.error("connectSync "+node.getIp()+" "+node.getPort());
			connectSync(); 
			ScheduledExecutor.getInstance().putClient(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public RedisClient(String host, int port, SelectorGroup selectorGroup) {
		super(host, port, selectorGroup);
		// TODO Auto-generated constructor stub
		this.codec(new ClientRedisCodec());
	}
	public	 Session frontSession = null;

	@Override
	protected void onSessionConnected(Session sess) throws IOException {
		// TODO Auto-generated method stub
		super.onSessionConnected(sess);
		sendPassword(); 
		//log.error("onSessionConnected:"+id);  
	}
	
	
	public void sendPassword(){
		if(!StringUtil.isEmpty(node.getPassword())){
			RedisCommand redisCommand = new RedisCommand();
			List<byte[]> list = new ArrayList<>();
			list.add("AUTH".getBytes());
			list.add(node.getPassword().getBytes());
			redisCommand.setArgCount(2);
			redisCommand.setArgs(list);
			try {
				this.sendPassword(redisCommand);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			isFisrt.set(true);
			//= true;
		}
	}
	@Override
	protected void onSessionRegistered(Session sess) throws IOException {
		// TODO Auto-generated method stub
		super.onSessionRegistered(sess);
		
	}
 	java.util.concurrent.atomic.AtomicBoolean isFisrt = new AtomicBoolean(false);
//	boolean isFisrt = false;
	
	ConcurrentLinkedQueue<RedisCommand> sendQueue = new ConcurrentLinkedQueue<>();

 	@Override
 	public void send(RedisCommand req) throws IOException {
 		// TODO Auto-generated method stub
 		
		if(!isFisrt.get()){
			//放队列
			sendQueue.add(req);
			return ; 
		}
		super.send(req);
 		//log.error("send "+id+" " +req.getArgString());
 	}
 	public void sendPassword(RedisCommand req) throws IOException {
 		// TODO Auto-generated method stub
 		connectSync();  
    	this.session.write(req);
 	}
	@Override
    protected void onMessage(Object obj, Session sess) throws IOException {  
	
		if(obj instanceof StatusRedisReply){
			//心跳消息
			StatusRedisReply statusRedisReply = (StatusRedisReply) obj;
			if(statusRedisReply.getValue() != null){
				String value = new String(statusRedisReply.getValue());
				if("PONG".equals(value)){
					heartbeatFlushTime = TimeUtil.currentTimeMillis();
					int used = ChannelManager.getInstacnce().getPool(node).getUse();
					int count = ChannelManager.getInstacnce().getPool(node).getCount();
					//如果   
				//	log.error("使用中 "+ used +" "+count);
					return ;
				} 
				if(!isFisrt.get()){
					if("OK".equals(value)){
						//
					//	log.error("密码正确 "+id);
 
 						isFisrt.set(true); 
						while(true){
							RedisCommand redisCommand = sendQueue.poll();
							if(redisCommand == null){
								break;
							}
							send(redisCommand);;
						}
				 	
					}
					return ;
				}
			}
			//StatusRedisReply
		}
		if(!isFisrt.get()){
			if(obj instanceof ErrorRedisReply){
				ErrorRedisReply  error =(ErrorRedisReply) obj;
 		 		log.error("还没连接上" +id +" "+obj +" "+new String(error.getValue()));
 
			}   
			return ;
			//isFisrt.set(true); 
		}
			
		if(frontSession != null){
			frontSession.write(obj);
			frontSession = null;
	    	 ChannelManager.getInstacnce().returnObject(this);;
		}
		/*
		if(obj instanceof BulkRedisReply){
			BulkRedisReply bulkRedisReply = (BulkRedisReply)obj;
		}if(obj instanceof MultyBulkRedisReply){
			MultyBulkRedisReply bulkRedisReply = (MultyBulkRedisReply)obj;
			System.out.println(bulkRedisReply.getCount()+" MU");
		}
		*/
	} 

	private int timeoutCount = 0;
	private long heartbeatFlushTime = TimeUtil.currentTimeMillis();
	private int timeoutTime = 80*1000;
	@Override
	public void heartbeat() {
		// TODO Auto-generated method stub
		if(this.hasConnected() && isFisrt.get()){
				//log.info("heartbeat 心跳 ");
			try { 
				RedisCommand redisCommand = new RedisCommand();
				List<byte[]> list = new ArrayList<>();
				list.add("PING".getBytes());
				redisCommand.setArgCount(1);
				redisCommand.setArgs(list);
				send(redisCommand);
				//sendMsg(this.getDataNodeConfig().getHeartbeatSQL());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/***
	 * 是否关闭
	 * @return
	 */
	public boolean  isClosed3(){
		boolean close = !this.hasConnected();
		return close;
	}
	public boolean  isClosed(){
		boolean close = !this.hasConnected();
		long nowTime =  TimeUtil.currentTimeMillis();
		if((nowTime - heartbeatFlushTime) >  timeoutTime ){
			log.error("连接超时 ,%s",this.toString());
			timeoutCount ++;
			if(timeoutCount>3){
				close = true;
			}
		}else{
			timeoutCount = 0;
		} 
		if(close){
			log.error("连接关闭 %s %s",node.getIp(),String.valueOf(node.getPort()) );
		}
		return close;
	}

	public Node getNode() {
		return node;
	}
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		super.close();
	}
	
	@Override
	protected void onSessionDestroyed(Session sess) throws IOException {
		// TODO Auto-generated method stub
		super.onSessionDestroyed(sess);
		log.error("onSessionDestroyed ");
		ChannelPool channelPool =  ChannelManager.getInstacnce().getPool(node);
		if(channelPool != null){
			channelPool.remove(this);
		}
	}
	public static void main(String[] args) throws Exception { 
		//IRedisReply
		//1) create a SelectorGroup just like EventLoopGroup in netty
		final SelectorGroup selectorGroup = new SelectorGroup();  
		//2) create a client, lazy connection if needed. 
	    Node dataNodeConfig = new Node("127.0.0.1:6379:123456");

		RedisClient redisClient = new RedisClient (dataNodeConfig, selectorGroup);
		RedisCommand  requestCommand = new RedisCommand();
		requestCommand.setArgCount(1);
		List<byte[]> list = new ArrayList<>();
		list.add("HGETALL".getBytes());
		list.add("h1".getBytes());
		
		requestCommand.setArgs(list);
		System.out.println(redisClient);
		// redisClient.send(requestCommand);
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	@Override
	public String toString() {
		return "RedisClient [id=" + id + "]";
	}
	
}
