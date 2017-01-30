package com.test;


import java.util.concurrent.atomic.AtomicLong;

import com.openlibin.client.pool.RedisClient;

import redis.clients.jedis.Jedis;

class Task extends Thread{
	private final Jedis client;
	private final AtomicLong counter;
	private final long startTime;
	private final long N;
	public Task(Jedis client, AtomicLong counter, long startTime, long N) {
		this.client = client;
		this.counter = counter;
		this.startTime = startTime;
		this.N = N;
	}
	@Override
	public void run() { 
		for(int i=0; i<N; i++){
			try {
				String value = "哈哈"+i;
				client.set("test"+i, "哈哈"+i);
				 
				String t = client.get("test"+i);
				if(!value.equals(t)){
					System.err.println("不存在"+t);
				}  
			//	System.out.println(t);
				counter.incrementAndGet();
			} catch (Exception e) { 
				e.printStackTrace();
			}
			if(counter.get()%5000==0){
				double qps = counter.get()*1000.0/(System.currentTimeMillis()-startTime);
				System.out.format("QPS: %.2f\n", qps);
			}
		}
	}
}

public class RemotingPerf {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {  
		int selectorCount =1;// Helper.option(args, "-selector", 1);
		int executorCount = 128;//Helper.option(args, "-executor", 128);
		final long N =  10000;
		final int threadCount =50;
	 
		final AtomicLong counter = new AtomicLong(0);
		 
		Jedis[] clients = new Jedis[threadCount];
		for(int i=0;i<clients.length;i++){
			 Jedis jedis = new Jedis("127.0.0.1",16379);
			clients[i] = jedis;
		}
		
		final long startTime = System.currentTimeMillis();
		Task[] tasks = new Task[threadCount];
		for(int i=0; i<threadCount; i++){
			tasks[i] = new Task(clients[i], counter, startTime, N);
		}
		for(Task task : tasks){
			task.start();
		} 
		
		//4）释放链接资源与线程池相关资源
		//client.close();
		//dispatcher.close();
	} 
	
}
