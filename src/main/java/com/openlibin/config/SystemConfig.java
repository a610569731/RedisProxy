package com.openlibin.config;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

public class SystemConfig {
	
	/*private  String serverId="";
	private  int maxCount=1000;

	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public int getMaxCount() {
		return maxCount;
	}
	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}
 */
	 private static SystemConfig instance = null;
	    private Map<String,Object> properties = null;
	    private SystemConfig() {
	    	
	    }
	    public void initProperties(String propertiePath){
	    	if(new File(propertiePath).exists()){
	    		InputStream in = null;
	    		Properties pro = new Properties();
	    		try{
					in = new FileInputStream(propertiePath);
					pro.load(in);
					Iterator<Object>  it = pro.keySet().iterator();
					while(it.hasNext()){ 
						String key = (String) it.next();
						String value = pro.getProperty(key).trim();
						System.out.println("key "+key+" values:"+value);
						put(key, value);
					}
	    		}catch(Exception ex){
	    			ex.printStackTrace();
	    		}finally {
					if(in != null){
						try {
							in.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace(); 
						}
					}
				}
	    	}
	    }
	    public  void put(String key,String value){

	    	if(properties == null){
	    		properties = new HashMap<String, Object>();
	    	}
	    	if(!"".equals(key)){
	    		properties.put(key, value);
	    	}
	    }
	    
	    public  void put(String key,Object value){

	    	if(properties == null){
	    		properties = new HashMap<String, Object>();
	    	}
	    	if(!"".equals(key)){
	    		properties.put(key, value);
	    	}
	    }
	  
	    public  String get(String key){ 

	    	if(properties == null){
	    		return null;
	    	}
	    	if(!"".equals(key)){
	    		if(properties!=null && properties.containsKey(key)){
		    		Object obj = properties.get(key);
		    		
		    		if(obj == null){
		    			return null;
		    		}else{
		    			return obj.toString();
		    		}
	    		}
	    	//	return properties.get(key).toString();
	    	}
	    	
	    	return null;
	    }
	    public  Object getObject(String key){ 

	    	if(properties == null){
	    		return null;
	    	}
	    	if(!"".equals(key)){
	    		return properties.get(key);
	    	}
	    	
	    	return null;
	    }
	    private static synchronized void syncInit() {
	      if ( instance == null) {
	        instance = new SystemConfig();
	        
	      }
	    }
	    public static SystemConfig getInstance() {
	      if (instance == null) {
	        syncInit();
	      }
	      return instance;
	    }
	    
	    public boolean containsKey(String key){
	    	if(key == null){
	    		return false;
	    	}
	    	return instance.properties.containsKey(key);
	    }
	    public void remove(String key){
	    	if(key == null){
	    		return  ;
	    	}
	    	instance.properties.remove(key);
	    }
	    public Map<String,Object> getProperties() {
	      return properties;
	    }
	    public void updateProperties() {
	      //Load updated configuration information by new a GlobalConfig object
	      SystemConfig shadow = new SystemConfig();
	      properties = shadow.getProperties();
	    }
	   public static void main(String[] args) {
		
		   HashMap<String,String> map  = new HashMap<String,String>();
		   map.put("s", "s");
		   
		   System.out.println(map.containsKey("s"));
	}
	
}
