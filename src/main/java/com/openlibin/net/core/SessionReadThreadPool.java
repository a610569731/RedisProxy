package com.openlibin.net.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

 
public class SessionReadThreadPool {
	private int maxPool;
	private int minPool;
	
	List<SessionReadThread> listThread ; 
	AtomicInteger acCurrent = new AtomicInteger(-1);
	Map<Session, SessionReadThread> mapThread = new ConcurrentHashMap<>();
	
	public SessionReadThreadPool(int maxPool,int minPool){
		if(maxPool == 0){
			maxPool =1;
		}
		this.maxPool = maxPool;
		this.minPool = minPool;
		listThread = new ArrayList<>(maxPool);
	
		for(int i=0;i<maxPool;i++){ 
			SessionReadThread sessionReadThread = new SessionReadThread();
			sessionReadThread.start();
			listThread.add(sessionReadThread);
		} 
	}
	public void put(Session session, Runnable theMsg){
		
		if(mapThread.containsKey(session)){
			SessionReadThread sessionReadThread =  mapThread.get(session);
			sessionReadThread.put(theMsg);
		}else{

			int index = acCurrent.incrementAndGet();
			if(index < maxPool){
			}else{
				index = 0;
				acCurrent.set(0); 
			}
			SessionReadThread sessionReadThread =  listThread.get(index);
			sessionReadThread.put(theMsg);
			mapThread.put(session, sessionReadThread);
		}
		
		//index = 0; 
		//SessionReadThread sessionReadThread =  listThread.get(index);
		//sessionReadThread.put(theMsg);
		
		
	}
	public void put( Runnable theMsg){
		int index = acCurrent.incrementAndGet();
		if(index < maxPool){
		}else{
			index = 0;
			acCurrent.set(0); 
		}
		 
		SessionReadThread sessionReadThread =  listThread.get(index);
		sessionReadThread.put(theMsg);
		
	}
	
	public static void main(String[] args) {
		final SessionReadThreadPool readThread = new SessionReadThreadPool(3, 3);
		 
		readThread.put(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("xx1");
			}
		});
		
		readThread.put(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("xx2");
			}
		});
		readThread.put(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("xx4");
			}
		});
		
readThread.put(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("xx5");
			}
		});

readThread.put(new Runnable() {
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("xx6");
	}
});


	}
  	
}
