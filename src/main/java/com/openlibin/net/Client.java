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
package com.openlibin.net;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.openlibin.net.core.IoAdaptor;
import com.openlibin.net.core.SelectorGroup;
import com.openlibin.net.core.Session;
import com.openlibin.net.kit.log.Logger;


/**
 * Client is a special type of IoAdaptor, it stands for a connection from client to server and
 * you can configure the way it response to the read/write/connected/disconnected/data etc. events.
 * 
 * Client requires a SeletorGroup to manage the underlying event loop messaging.
 * You have to close the client once it is desired to be released, since the client resides a
 * session and a heartbeator routine.
 * 
 *
 * @param <REQ> request message type
 * @param <RES> response message type
 */
public class Client<REQ, RES> extends IoAdaptor implements Closeable {
	private static final Logger log = Logger.getLogger(Client.class); 
	
	protected final SelectorGroup selectorGroup;  
	protected String host = "127.0.0.1";
	protected int port = 15555;

	protected int readTimeout = 3000;
	protected int connectTimeout = 3000;
	//心跳
	protected int heartbeatInterval = 60000; // 60s

	protected ConcurrentMap<String, Object> attributes = null;
	//protected final ScheduledExecutorService heartbeator = Executors.newSingleThreadScheduledExecutor();

	protected Session session; 
	
	protected volatile MsgHandler<RES> msgHandler; 
	protected volatile ErrorHandler errorHandler;
	protected volatile ConnectedHandler connectedHandler;
	protected volatile DisconnectedHandler disconnectedHandler;
	public Client(SelectorGroup selectorGroup){
		this.selectorGroup = selectorGroup;
	}
	public Client(String address, SelectorGroup selectorGroup) { 
		String[] blocks = address.split("[:]");
		if(blocks.length != 2){
			throw new IllegalArgumentException(address + " is invalid address");
		}
		this.host = blocks[0];
		this.port = Integer.valueOf(blocks[1]);
		this.selectorGroup = selectorGroup; 
		
		selectorGroup.start(); 
		//TODO 优化 心跳 统一管理
		/*
		this.heartbeator.scheduleAtFixedRate(new Runnable() {
			public void run() {
				heartbeat();
			}
		}, heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
		*/
	//	MySqlServer.getInstance().getScheduledExecutor().putClient(this);
		 
	}

	public Client(String host, int port, SelectorGroup selectorGroup) { 
		this(String.format("%s:%d", host, port), selectorGroup);
	}

	 
	public void heartbeat() {
		
	}

	public boolean hasConnected() {
		return session != null && session.isActive();
	}

	public void connectSync() throws IOException {
		if (!this.hasConnected()) {
			// 同步进行连接操作
			connectAsync();
			this.session.waitToConnect(this.connectTimeout);
		}
	}

	public   void connectAsync() throws IOException {
		if (this.session != null) {
			if (this.session.isActive() || this.session.isNew()) {
				return;
			}
		}
		this.session = selectorGroup.registerClientChannel(host, port, this);
	}
	
	@Override
	protected void onSessionConnected(Session sess) throws IOException { 
		super.onSessionConnected(sess);
		if(this.connectedHandler != null){
			this.connectedHandler.onConnected(sess);
		} else {
			log.info("Connected: "+sess);
		}
	}
	
	public void send(REQ req) throws IOException{
    	connectSync(); 
    	this.session.write(req);
    } 
	
	@Override
    protected void onMessage(Object obj, Session sess) throws IOException {  
		@SuppressWarnings("unchecked")
		RES res = (RES)obj; 
    	if(msgHandler != null){
    		this.msgHandler.handle(res, sess);
    		return;
    	}
    	
    	log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!Drop,%s", res);
	} 
	 
	@Override
	protected void onException(Throwable e, Session sess) throws IOException {
		if(e instanceof IOException && this.errorHandler != null){
			this.errorHandler.onError((IOException)e, sess);
		} else {
			super.onException(e, sess);
		}
	}
	
	@Override
	protected void onSessionDestroyed(Session sess) throws IOException { 
		super.onSessionDestroyed(sess);
		if(disconnectedHandler != null){
			disconnectedHandler.onDisconnected();
		}
	}
	
	public void onMessage(MsgHandler<RES> msgHandler){
    	this.msgHandler = msgHandler;
    }
    
    public void onError(ErrorHandler errorHandler){
    	this.errorHandler = errorHandler;
    } 
    
    public void onConnected(ConnectedHandler connectedHandler){
    	this.connectedHandler = connectedHandler;
    } 
    
    public void onDisconnected(DisconnectedHandler disconnectedHandler){
    	this.disconnectedHandler = disconnectedHandler;
    } 

	@Override
	public void close() throws IOException {
		//this.heartbeator.shutdown();
		this.onDisconnected(null); //clear disconnection handler
		if (this.session != null) {
			this.session.close();
		} 
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Session getSession() {
		return session;
	}
	
	public ExecutorService getExecutorService(){
		return this.selectorGroup.getExecutorService();
	}

	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(int heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	@SuppressWarnings("unchecked")
	public <V> V attr(String key) {
		if (this.attributes == null) {
			return null;
		}

		return (V) this.attributes.get(key);
	}

	public <V> void attr(String key, V value) {
		if (this.attributes == null) {
			synchronized (this) {
				if (this.attributes == null) {
					this.attributes = new ConcurrentHashMap<String, Object>();
				}
			}
		}
		this.attributes.put(key, value);
	}
	
	public static interface ConnectedHandler { 
		void onConnected(Session sess) throws IOException;   
	}
	
	public static interface DisconnectedHandler { 
		void onDisconnected() throws IOException;   
	}
	
	public static interface ErrorHandler { 
		void onError(IOException e, Session sess) throws IOException;   
	}
	
	public static interface MsgHandler<T> { 
		void handle(T msg, Session sess) throws IOException;   
	}
 


}
