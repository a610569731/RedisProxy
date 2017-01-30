/**
 * 
 */
package com.test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;

/**
 * @author liubing
 *
 */
public class RedisClientDemo {

	public static char getChar(byte bytes) {
        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (1);
        bb.put (bytes);
        bb.flip ();
         CharBuffer cb = cs.decode (bb);
    
         char [] tmp = cb.array();
           
     return tmp[0];
  }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		byte b = 13;
		System.out.println((char)(b));
		
		for(int j=0;j<1;j++){
			Jedis  redis = new Jedis ("127.0.0.1",16379);
		 	String d = redis.auth("123456");
		 	System.out.println(d); 
		 	
		 	if(false){
		 		String s = redis.hget("user:"+1001,"state");
		 		
		 		System.out.println("s:"+s);
		 		return ;
		 		//RedisCache.hget("user:"+userId, "state")
		 	}
		 	
		 	for(int k=0;k<80;k++){
				  redis.hset("testmap6", ""+k, k+"大沙发沙发沙发非沙发沙大沙发沙发沙发沙发沙发沙发沙发沙发沙发发沙发沙发分身乏术方法");
			}
 		 	
			Map<String,String> mapAll =  redis.hgetAll("testmap6");
			System.out.println(mapAll);   
			for(int i=0;i<-1;i++){
			//	redis.ping();
				//redis.set("s1", "hg_"+i);    
				//System.out.println( "x:"+redis.get("s1"));
			 
				for(int k=0;k<10;k++){
				//	 redis.hset("h2", ""+k, "1");
				}
				redis.hgetAll("user:1001");
		//	redis.quit();
			}
			redis.close();
		}
		//redis.set("s1", "xxxxx");     
		//redis.set("s2", "xxxxx2");   
		//redis.ping();
		//System.out.println( "x:"+redis.get("s2"));

	//	 redis.close();
		 //redis.slaveofNoOne();
		//System.out.println(redis.get("name"));
		
		
		//redis.set("name", "liubing1");
		//redis.expire("name", 10);
		
//		for(int z=0;z<5;z++){
//			long startTime=System.currentTimeMillis();
//			for(int j=0;j<1000;j++){
//				for(int i=0;i<100;i++){
//					//System.out.println(i);
//					redis.get("name");
//					//redis.set("name", "liubing1");
//				}
//			}
//			long endTime=System.currentTimeMillis();
//			System.out.println("完成时间:"+(endTime-startTime));
//		}
		
	}

}
