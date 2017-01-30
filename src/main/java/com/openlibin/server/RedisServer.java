package com.openlibin.server;

import java.io.IOException;

import com.openlibin.net.Server;
import com.openlibin.net.core.SelectorGroup;
import com.openlibin.net.core.SelectorThread;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.redis.core.server.LBRedisServerHandler;
import com.openlibin.server.coder.RequestRedisAdaptor;


public class RedisServer {
	private static final Logger log = Logger.getLogger(SelectorThread.class);

	public void init(int port) throws IOException{
		final SelectorGroup dispatcher = new SelectorGroup();
	 	dispatcher.selectorCount(2);
	  
		final Server server = new Server(dispatcher);
		RequestRedisAdaptor ioAdaptor = new RequestRedisAdaptor();
		log.error("start port "+port);

		server.start(port, ioAdaptor);
	}
	
	public static void main(String[] args) throws IOException {
		RedisServer redisServer = new RedisServer();
		redisServer.init(16379);
	}
}
