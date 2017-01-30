package com.openlibin.server.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openlibin.net.core.Session;
import com.openlibin.net.kit.log.Logger;

public class RedisSessionManager {
	private static Map<Session,RedisSession> map = new ConcurrentHashMap<Session,RedisSession>();
	private static Map<Long,RedisSession> idMap = new ConcurrentHashMap<Long,RedisSession>();
	private static final Logger log = Logger.getLogger(RedisSessionManager.class); 
	
	public static void remove(Session key ){
		if(map.containsKey(key)){
			
			RedisSession mysqlSession =  map.remove(key);
			idMap.remove(mysqlSession.getProcessId());
			//log.info("remove session %d ",idMap.size());
		}
	
	}
	
	public static void put(Session key,RedisSession mysqlSession){
		map.put(key, mysqlSession);
		idMap.put(mysqlSession.getProcessId(), mysqlSession);
	}
	public static RedisSession get(Session key){
		return map.get(key); 
	}
	public static Map<Long, RedisSession> getIdMap() {
		return idMap;
	}
	public static void setIdMap(Map<Long, RedisSession> idMap) {
		RedisSessionManager.idMap = idMap;
	}
	 
}
