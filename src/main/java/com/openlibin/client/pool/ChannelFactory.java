package com.openlibin.client.pool;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;

import com.openlibin.net.core.SelectorGroup;
import com.openlibin.slice.Node;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory  ;
import org.apache.commons.pool2.PooledObject  ;
import org.apache.commons.pool2.impl.DefaultPooledObject  ;
import org.apache.commons.pool2.*; 

public class ChannelFactory  implements PooledObjectFactory<RedisClient>   {
	private SelectorGroup selectorGroup;  
	private boolean ownSelectorGroup = false;
 	private Node  dataNodeConfig ;
	public ChannelFactory(Node dataNodeConfig){
		
		this.dataNodeConfig = dataNodeConfig;
		this.selectorGroup = new SelectorGroup().selectorCount(5).excutorClientCount(5);
		this.ownSelectorGroup = true;
		
	}
	@Override
	public void activateObject(PooledObject<RedisClient> arg0)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroyObject(PooledObject<RedisClient> obj)
			throws Exception {
		// TODO Auto-generated method stub
		obj.getObject().close(); 
	}

	@Override
	public PooledObject<RedisClient> makeObject() throws Exception {
		// TODO Auto-generated method stub
		RedisClient b =   new RedisClient(dataNodeConfig, selectorGroup);
		return new DefaultPooledObject<RedisClient>(b);
	}
	
	/**
	 * 取出时调用
	 */
	@Override
	public void passivateObject(PooledObject<RedisClient> obj)
			throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("passivateObject ");
	}
	
	//
	@Override
	public boolean validateObject(PooledObject<RedisClient> obj) {
		// TODO Auto-generated method stub
		//System.out.println("validateObject");
		RedisClient myConn = obj.getObject();  
		
 		return   !myConn.isClosed();
	}
	  
}  