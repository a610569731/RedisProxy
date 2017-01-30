package com.openlibin.net.core;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import com.openlibin.net.ext.BarrierHolder;
import com.openlibin.net.ext.OptimisticQueue;
 

public class SessionReadThread extends Thread implements BarrierHolder{
	//private BlockingDeque<Runnable> queue = new LinkedBlockingDeque<Runnable>();
	OptimisticQueue<Runnable> queue = new OptimisticQueue<Runnable>(10);
	private Object barrier = new Object();

	//LinkedBlockingQueue用法
	public void put(Runnable theMsg){
		try {
			queue.offer(this, theMsg); 
		} catch (Exception e) {
 			e.printStackTrace();
		}
	}
	 @Override
	public void run() {
 		super.run();
		while(true){ 
			Runnable theMsg =null;
			try {
				theMsg = queue.take(this);
				//theMsg = queue.(); //take
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 			if(theMsg != null){
 				//System.out.println("theMsg "+theMsg);
				theMsg.run();
			} 
		}
	}
	@Override
	public Object getBarrier() {
		// TODO Auto-generated method stub
		return barrier;
	}
}
