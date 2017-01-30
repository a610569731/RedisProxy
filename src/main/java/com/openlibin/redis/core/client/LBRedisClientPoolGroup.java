package com.openlibin.redis.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.openlibin.slice.Node;
import com.openlibin.slice.Shard;

/**
 * 参考：
 * http://www.codeproject.com/Articles/153898/Yet-another-implementation-of-a-lock-free-circular
 */
public class LBRedisClientPoolGroup {

	// 环状缓存
	ConcurrentLinkedQueue<LBRedisClient> queue = new ConcurrentLinkedQueue<>();
	public static ConcurrentHashMap<Node, LBRedisClientPool> map = new ConcurrentHashMap<>();
	public static Shard<Node> nodes  = null;
	public static void  init(String hosts){
		String arr[] = hosts.split(";");
		List<Node> list = new ArrayList<>();
		
		for(String str : arr){
			Node node = new Node(str);
			System.out.println(node);
			
			list.add(node);
			//nodes.add(node)
		}
		nodes = new  Shard<Node> (list);
		
	}
	
	public static LBRedisClientPool get(String key){
		 
		Node d = nodes.getShardInfo(key);
		LBRedisClientPool lbRedisClientPool =   map.get(d);
		if(lbRedisClientPool == null){  
			lbRedisClientPool =  new LBRedisClientPool(100,d.getIp(), d.getPort(), d.getPassword());
			map.put( d, lbRedisClientPool); 
		}       
		return lbRedisClientPool;
	}
	
}
