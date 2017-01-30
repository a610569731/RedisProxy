package com.openlibin;

import java.io.File;
import java.io.IOException;

import com.openlibin.client.pool.ChannelManager;
import com.openlibin.config.SystemConfig;
import com.openlibin.net.Server;
import com.openlibin.net.core.SelectorGroup;
import com.openlibin.net.core.SelectorThread;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.redis.core.client.LBRedisClientPoolGroup;
import com.openlibin.redis.core.server.LBRedisServer;
import com.openlibin.schedule.ScheduledExecutor;
import com.openlibin.server.RedisServer;
import com.openlibin.server.coder.RequestRedisAdaptor;
import com.openlibin.slice.Node;
import com.openlibin.util.ConfigKit;
import com.openlibin.util.MyPath;
import com.openlibin.util.StringUtil;

public class Start {
	private static final Logger log = Logger.getLogger(SelectorThread.class);
 
	 
	public void init(int port) throws IOException{
		final SelectorGroup dispatcher = new SelectorGroup();
	 	dispatcher.selectorCount(2);
	  
		final Server server = new Server(dispatcher);
		RequestRedisAdaptor ioAdaptor = new RequestRedisAdaptor();
		log.error("start port "+port);

		ScheduledExecutor.getInstance();
		
		server.start(port, ioAdaptor);
	}
	 
	public static void main(String[] args) throws IOException {
		
		String xmlConfigFile = ConfigKit.option(args, "-conf", "system.properties");
		String root= MyPath.getProjectPath();
		 
		String path =  root+File.separator+ xmlConfigFile;
		 
		File config = new File(path);
		if(config.exists()){
			log.error("初始化配置文件");
			SystemConfig.getInstance().initProperties(path);
		}else{ 
			log.error("初始化配置文件error");
		}
		
		
		int port = StringUtil.parseInt(SystemConfig.getInstance().get("system.port"));
		if(port <= 0){
			log.error("port is 0");
			return ;
		}
		//连接后端的 ip 端口 密码  
		String nodes = SystemConfig.getInstance().get("system.nodes");
		
		
		Start redisServer = new Start();
		//String nodes = "127.0.0.1:6379:123456"; //127.0.0.1:6379:123456;
		ChannelManager.getInstacnce().initNodes(nodes);
		//前端端口
		redisServer.init(port);
	}
}
