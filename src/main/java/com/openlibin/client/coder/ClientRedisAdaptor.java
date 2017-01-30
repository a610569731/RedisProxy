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
package com.openlibin.client.coder;

import java.io.IOException;

import com.openlibin.net.core.IoAdaptor;
import com.openlibin.net.core.Session;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.reply.IRedisReply;
import com.openlibin.redis.core.reply.impl.BulkRedisReply;
import com.openlibin.redis.core.reply.impl.MultyBulkRedisReply;
import com.openlibin.redis.core.reply.impl.StatusRedisReply;
import com.openlibin.server.session.RedisSessionManager;

public class ClientRedisAdaptor extends IoAdaptor{   
	//全局处理器，优先级最高
	//根据cmd处理，优先级次之
//	protected Map<String, MysqlMessageHandler> cmdHandlerMap = new ConcurrentHashMap<String, MysqlMessageHandler>();
	private static final Logger log = Logger.getLogger(ClientRedisAdaptor.class); 


	public ClientRedisAdaptor(){
		codec(new ClientRedisCodec()); //个性化消息编解码
	 
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
    public void onMessage(Object obj, Session sess) throws IOException {  
    	IRedisReply msg = (IRedisReply)obj;   
    	System.out.println("收到了"+msg);
    	
    	/*
    	String path = msg.getRequestPath(); //requestPath
    	if(path == null){ 
    		Message res = new Message();
    		res.setId(msgId); 
        	res.setResponseStatus(400);
        	res.setBody("Bad Format(400): Missing Command and RequestPath"); 
        	sess.write(res);
    		return;
    	}
    	
    	MessageProcessor uriHandler = uriHandlerMap.get(path);
    	if(uriHandler != null){
    		Message res = null; 
    		try{
    			res = uriHandler.process(msg); 
	    		if(res != null){
	    			res.setId(msgId);
	    			if(res.getResponseStatus() == null){
	    				res.setResponseStatus(200);// default to 200
	    			}
	    			sess.write(res);
	    		}
    		} catch (IOException e){ 
    			throw e;
    		} catch (Exception e) { 
    			res = new Message();
    			res.setResponseStatus(500);
    			res.setBody("Internal Error(500): " + e);
    			sess.write(res);
			}
    
    		return;
    	} 
    	
    	Message res = new Message();
    	res.setId(msgId); 
    	res.setResponseStatus(404);
    	String text = String.format("Not Found(404): %s", path);
    	res.setBody(text); 
    	sess.write(res); */
    } 

    @Override
    protected void onSessionDestroyed(Session sess) throws IOException {
    	// TODO Auto-generated method stub
    	super.onSessionDestroyed(sess); 
    	log.info("onSessionDestroyed");
    	RedisSessionManager.remove(sess);
    }
}

