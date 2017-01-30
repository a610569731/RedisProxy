package com.openlibin.redis.core.client;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 参考：
 * http://www.codeproject.com/Articles/153898/Yet-another-implementation-of-a-lock-free-circular
 */
public class LBRedisClientPool {

	// 环状缓存
	ConcurrentLinkedQueue<LBRedisClient> queue = new ConcurrentLinkedQueue<>();
	private final AtomicInteger count = new AtomicInteger(0);
	private final AtomicInteger size = new AtomicInteger(0);

	private String ip;
	private int port;
	private String password;
	
	private int  maxCount;
	LBRedisClient lbRedisClientCurrent =   null;
	public LBRedisClientPool(int maxCount,String ip,int port,String password) {
		if (maxCount < 0){
			throw new IllegalArgumentException("Illegal capacity " + maxCount);
		}
		this.maxCount = maxCount;
		this.ip = ip;
		this.port = port;
		this.password = password;
		   
	//	LBRedisClient lbRedisClient =  pop();
	//	push(lbRedisClient);
		lbRedisClientCurrent = 	 new LBRedisClient(ip, port, password);
		lbRedisClientCurrent.open();     
		lbRedisClientCurrent.setLbRedisClientPool(this);

	}
   
	public boolean push(LBRedisClient e) {
		 
		boolean b =queue.offer(e);
		if(b){
			count.incrementAndGet();
		}   
		return b;
	}

	public LBRedisClient pop() {
		if(false){
			return lbRedisClientCurrent;
		}
		 LBRedisClient lbRedisClient = queue.poll();
		 if(lbRedisClient != null){
			count.decrementAndGet();
			 return lbRedisClient;
		 }
		 if(size.get() >= maxCount ){
			 System.err.println("Illegal capacity  "+maxCount);
			 throw new IllegalArgumentException("Illegal capacity " + maxCount);
			//return lbRedisClientCurrent;
		 }
		 /*
		 if(count.get() >=maxCount ){
			throw new IllegalArgumentException("Illegal capacity " + maxCount);
		 }*/
		 LBRedisClient lBRedisClient = 	 new LBRedisClient(ip, port, password);
		 lBRedisClient.open();
		 lBRedisClient.setLbRedisClientPool(this);
		 size.incrementAndGet();
		
		 System.out.println("创建 "+ size.get());
   
		 return lBRedisClient;
	} 
  
	public int size() {
		return queue.size();
	}

	public void clear() {
		while (size() > 0)
			pop();   
	}
	  
	@Override
	public String toString() {
		return "LBRedisClientPool [ip=" + ip + ", port=" + port + "]";
	}

	public static void main(String[] args) {
		 LBRedisClientPool lbRedisClientPool = new LBRedisClientPool(10, "192.168.5.140", 6379, "123456");

	}
 
}
