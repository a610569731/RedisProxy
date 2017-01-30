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
package com.openlibin.server.coder;

import java.io.IOException;

import com.openlibin.client.pool.ChannelManager;
import com.openlibin.client.pool.RedisClient;
import com.openlibin.net.core.IoAdaptor;
import com.openlibin.net.core.SelectorGroup;
import com.openlibin.net.core.Session;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.reply.impl.BulkRedisReply;
import com.openlibin.redis.core.reply.impl.ErrorRedisReply;
import com.openlibin.redis.core.reply.impl.MultyBulkRedisReply;
import com.openlibin.redis.core.reply.impl.StatusRedisReply;
import com.openlibin.server.session.RedisSessionManager;
import com.openlibin.slice.Node;

public class RequestRedisAdaptor extends IoAdaptor{   
	//全局处理器，优先级最高
	//根据cmd处理，优先级次之
//	protected Map<String, MysqlMessageHandler> cmdHandlerMap = new ConcurrentHashMap<String, MysqlMessageHandler>();
	private static final Logger log = Logger.getLogger(RequestRedisAdaptor.class); 
	final SelectorGroup selectorGroup = new SelectorGroup();  
	//2) create a client, lazy connection if needed. 
	
	//RedisClient redisClient = new RedisClient ("127.0.0.1", 6379, selectorGroup);
	public RequestRedisAdaptor(){
		codec(new RequestRedisCodec()); //个性化消息编解码
	}
	 
    public void onAccpet(Session sess){  
		
    }
  
    @Override
    protected void onSessionRegistered(Session sess) throws IOException {
    	// TODO Auto-generated method stub
    	super.onSessionRegistered(sess);
    	/*
    	HandshakePacket handshakePacket = new HandshakePacket();
		handshakePacket.protocolVersion = 10;
		handshakePacket.serverCapabilities = 63487;
		handshakePacket.serverCharsetIndex = 33;
		handshakePacket.serverStatus = 2;
		handshakePacket.threadId = 10;
		handshakePacket.serverVersion = "5.5.33a-MariaDB-log".getBytes();
		String strseed = StringUtil.getRandomString(8);
		String strrestOfScrambleBuff = StringUtil.getRandomString(12);
		MysqlSession session =  new MysqlSession(sess);
		session.setMysqlSeed(strseed.getBytes("ISO-8859-1"));
		session.setMysqlRestOfScrambleBuff(strrestOfScrambleBuff.getBytes("ISO-8859-1"));

		
		handshakePacket.seed = session.getMysqlSeed() ;// session.getMysqlSeed();// strseed.getBytes("ISO-8859-1");

		handshakePacket.restOfScrambleBuff =session.getMysqlRestOfScrambleBuff();// strrestOfScrambleBuff
				//.getBytes("ISO-8859-1");
		session.setHost(sess.getRemoteAddress());
    	MysqlSessionManager.put(sess, session);
		MysqlServerMessage msg = handshakePacket.write2();
		
		 
    	sess.write(msg);
    	*/
    }
    @Override
    protected void onSessionAccepted(Session sess) throws IOException {
    	// TODO Auto-generated method stub
    	super.onSessionAccepted(sess);
    	
    	
    }
   // public static Node dataNodeConfig = new Node("127.0.0.1:6379:123456");
    
    public void sendBack(Session sess,RedisCommand req){
     	RedisClient redisClient =  null;
     	
    	if(req.getArgCount() >= 2){
    		redisClient = ChannelManager.getInstacnce().get(new String(req.getArgs().get(1)));
    	}else{
    		redisClient = ChannelManager.getInstacnce().get("1");
    	}
    	if(redisClient == null){
    		ErrorRedisReply errorRedisReply = new ErrorRedisReply("back client error".getBytes());
    		try {
				sess.write(errorRedisReply);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();     
			}
    		return  ;
    	}
    	redisClient.frontSession = sess;
    	
    	try {
			redisClient.send(req);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		};
    }
    public void onMessage(Object obj, Session sess) throws IOException {  
    	RedisCommand msg = (RedisCommand)obj;  
    	if(msg.getArgCount()>=1){
    		String command = new String( msg.getArgs().get(0));
        	//System.out.println("收到了 "+command); 
    		 
    		if("AUTH".equals(command)){
    			StatusRedisReply statusRedisReply = new StatusRedisReply();
    	    	statusRedisReply.setValue("ok".getBytes());
    	    	sess.write(statusRedisReply);
    	    	return ;
    		}
    		else if("PING".equals(command)){
	    		//PONG
				StatusRedisReply statusRedisReply =  new StatusRedisReply("PONG".getBytes());
    	    	sess.write(statusRedisReply);

	    		return ;
	    	}else if("QUIT".equals(command)){
	    		//PONG
				StatusRedisReply statusRedisReply =  new StatusRedisReply("OK".getBytes());
    	    	sess.write(statusRedisReply);
 
	    		return ;
	    	}
    		sendBack(sess, msg);
			//redisClient.send(msg);
    		if("GET".equals(command)){
    			//redisClient.send(msg);
    		//	BulkRedisReply statusRedisReply = new BulkRedisReply("ok".getBytes());
    	    	//sess.write(statusRedisReply);  
    		}else if("HGETALL".equals(command)){
    			/*
    			MultyBulkRedisReply multyBulkRedisReply = new MultyBulkRedisReply();
    			
    			BulkRedisReply reply1 = new BulkRedisReply("username".getBytes());
    			BulkRedisReply reply2 = new BulkRedisReply("libin".getBytes());

    			multyBulkRedisReply.addReply(reply1);
    			multyBulkRedisReply.addReply(reply2);
    			*/
    			
    		}
    		
    	}
     
    } 

    @Override
    protected void onSessionDestroyed(Session sess) throws IOException {
    	// TODO Auto-generated method stub
    	super.onSessionDestroyed(sess); 
    	log.error("onSessionDestroyed");
    	RedisSessionManager.remove(sess);
    }
}

