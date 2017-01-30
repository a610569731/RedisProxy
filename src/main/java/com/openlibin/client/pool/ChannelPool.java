package com.openlibin.client.pool;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.RuntimeErrorException;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.openlibin.net.core.SelectorGroup;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.slice.Node;

public class ChannelPool {
	private static final Logger log = Logger.getLogger(ChannelPool.class);
	public String id = UUID.randomUUID().toString();
//	ChannelPool instacnce;
	//GenericObjectPool<RedisClient> pool;
	ConcurrentLinkedQueue<RedisClient> list = new ConcurrentLinkedQueue<>();
	
	AtomicInteger use = new AtomicInteger(0);
	AtomicInteger count = new AtomicInteger(0);
	int maxCount = 100; //最多
	int minCount = 10;//最少多少个
	
	int selectorCount = 5 ;
	int excutorClientCount = 5;
	
	//select 
	SelectorGroup	 selectorGroup = null;
	AtomicBoolean acCheckAll = new AtomicBoolean(false);
	public void checkAll(){
		if(acCheckAll.get()){
			return ;
		}
		acCheckAll.set(true);

		int size = count.get();
		for(int i=0;i<size;i++){
			RedisClient redisClient = list.poll();
			if(redisClient == null){
				break;
			}else{
				count.decrementAndGet();
				if(redisClient.isClosed()){
					redisClient = null;
				}
			}
		}
		acCheckAll.set(false);
		log.error("销毁 "+size);
	}
	
	public RedisClient get(){
		try {
			int countGet = 0;   
			while(true){
				RedisClient redisClient = list.poll();
				if(redisClient == null){
					if( count.get() >= maxCount){   
						log.error("连接数不够"); 
						
					}else{
						redisClient = new RedisClient(dataNodeConfig, selectorGroup);
						if(redisClient != null && !redisClient.isClosed()){
							count.incrementAndGet();
						}   
					}
				} 
				if(redisClient.isClosed()){
					//触发检查
					//TODO
					countGet++; 
					if(countGet >= 3){
						log.error("redisClient countGet 3 close"); 
						checkAll();
						break;  
					}
					log.error("redisClient close"); 

					continue;
				} 
				use.addAndGet(1);
				return redisClient;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void returnObject(RedisClient backendClient){
		int test = use.decrementAndGet();
		list.add(backendClient);
		
	}
	private Node dataNodeConfig;
	public ChannelPool(Node dataNodeConfig,SelectorGroup selectorGroup){
 		 this.dataNodeConfig = dataNodeConfig;
 		 this.selectorGroup = selectorGroup;
 		 try {
 			RedisClient redisClient = get();
 			returnObject(redisClient);
			//init();
		} catch (Exception e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 	
	public int getUse(){
		return use.get();
	}
	public int getCount(){
		return count.get();
	}
	
	
	
	public int getMinCount() {
		return minCount;
	}

	public void setMinCount(int minCount) {
		this.minCount = minCount;
	}

	
	public void check(){
		int used = getUse();
		int tempCount =	getCount();
		if(used < tempCount/2 && tempCount > minCount){
			//去除空闲的
			RedisClient redisClient = get();
			if(redisClient != null){
				try {
					redisClient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				count.decrementAndGet();
				use.decrementAndGet();
			}
		}
		used =  getUse();
		int count =  getCount();
		//如果 
		 
		log.error("检查后 使用中 "+ used +" "+count);
		
	}
	public void remove(RedisClient redisClient){
		if(list.remove(redisClient)){
			count.decrementAndGet();
		}
	}
	public static void main(String[] args) throws Exception {
		//BackendClientManager.getInstacnce().returnObject(b);;
		
	 
	}
}
