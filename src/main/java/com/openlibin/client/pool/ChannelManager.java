package com.openlibin.client.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.openlibin.net.core.SelectorGroup;
import com.openlibin.redis.core.client.LBRedisClientPool;
import com.openlibin.slice.Node;
import com.openlibin.slice.Shard;

public class ChannelManager {
	static ChannelManager instacnce;
	ConcurrentHashMap<Node,ChannelPool>  nodePoolMap  = new ConcurrentHashMap<>();
	
	public  Shard<Node> nodes  = null;

	int selectorCount = 5 ;
	int excutorClientCount = 5;
	SelectorGroup	 selectorGroup = new SelectorGroup().selectorCount(selectorCount).excutorClientCount(excutorClientCount);

	public  void  initNodes(String hosts){
		String arr[] = hosts.split(";");
		List<Node> list = new ArrayList<>();
		
		for(String str : arr){
			Node node = new Node(str);
			System.out.println(node);
			
			list.add(node);
			ChannelPool channelPool = new ChannelPool(node,selectorGroup);
			nodePoolMap.putIfAbsent(node, channelPool);
		}
		nodes = new  Shard<Node> (list);
	}
	public   RedisClient get(String key){
		Node dataNodeConfig = nodes.getShardInfo(key);
	//	System.out.println(dataNodeConfig);
		RedisClient redisClient = get(dataNodeConfig);
		
		return redisClient;
	}
	public static ChannelManager getInstacnce() {
		if (instacnce == null) {
			try {
				instacnce = init();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instacnce;
	}    
	/*** 
	 * 
	 * @param node 节点名称
	 * @return
	 */ 
	public RedisClient get(Node dataNodeConfig){
		
		try {
			ChannelPool channelPool = nodePoolMap.get(dataNodeConfig);

			if(channelPool!= null){ //如果不在上面初始化 
				return channelPool.get();
			}else{
				//这个地方会重复初始化
				channelPool = new ChannelPool(dataNodeConfig,selectorGroup);
				nodePoolMap.putIfAbsent(dataNodeConfig, channelPool);
				return channelPool.get();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public ChannelPool getPool(Node dataNodeConfig){
		try {
			
			ChannelPool channelPool = nodePoolMap.get(dataNodeConfig);
			if(channelPool != null){

			}else{
				 channelPool = new ChannelPool(dataNodeConfig,selectorGroup);
				 nodePoolMap.putIfAbsent(dataNodeConfig, channelPool);
			}
			return channelPool ;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/***
	 * 获取已使用
	 * @param node
	 * @return
	 */
 	public int getActiveCount(Node dataNodeConfig){
 		ChannelPool channelPool  = null;
		if(nodePoolMap.containsKey(dataNodeConfig)){
			channelPool = nodePoolMap.get(dataNodeConfig) ;

		}else{
			return 0;
		}
		return channelPool.getUse();
	}
	
	
	public void returnObject( RedisClient backendClient){
		nodePoolMap.get(backendClient.getNode()).returnObject(backendClient);
	}

	public static synchronized ChannelManager init() throws Exception {
		 
		ChannelManager backendClientManager = new ChannelManager();
		 
		return backendClientManager;
	} 
	
	
	public ConcurrentHashMap<Node, ChannelPool> getNodePoolMap() {
		return nodePoolMap;
	}
 
	public static void main(String[] args) throws Exception {
	    Node dataNodeConfig = new Node("127.0.0.1:6379:123456");

		ChannelManager.getInstacnce().get(dataNodeConfig);
		//BackendClientManager.getInstacnce().returnObject(b);;
		
	 
	}
}
