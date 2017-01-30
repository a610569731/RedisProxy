package com.openlibin.comm.log;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Priority;

public class LogAppender extends DailyRollingFileAppender {
	 private long maxFileSize;
	 private int maxBackupIndex;
	 
 

	 public void setMaxFileSize(String maxFileSize)  {  
        //默认 10M  
        Long size =  1024 * 1024 * 10L ;  
         
        maxFileSize = maxFileSize.toLowerCase();  
          
        if(maxFileSize.endsWith("kb")){  
            size = Long.valueOf(maxFileSize.replaceAll("kb", ""))*1024;  
        }else if (maxFileSize.endsWith("m")){  
            size = Long.valueOf(maxFileSize.replaceAll("m", ""))*1024*1024;  
        }else {  
            size =  1024 * 1024 * 10L ;  
        }  
        this.maxFileSize = size;  
    }  

	public int getMaxBackupIndex() {
		return maxBackupIndex;
	}

	public void setMaxBackupIndex(int maxBackupIndex) {
		
		this.maxBackupIndex = maxBackupIndex;
		String fileName = getFile();
		File file = new File(fileName);
		String name = file.getName();
		if(file.exists()){
			String parentDir = file.getParent();
			deleteFileLog(parentDir,getName(name));
		}else{
			boolean b = false;
			try {
				b = file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(b){
				String parentDir = file.getParent();
				deleteFileLog(parentDir,getName(name));
			} 
		}
	}
	public String getName(String name){
		if(name.indexOf(".") != -1){
			name = name.substring(0,name.indexOf("."));
		}
		return name;
	}
	public void deleteFileLog(String parentDir,String name){
 		long nowTime = System.currentTimeMillis();
		File file = new File(parentDir);
		if(file.exists()){
			File[] files =  file.listFiles();
			for(File f: files){
			    long time = f.lastModified();
 		        long cha = (nowTime - time );
		        if(cha > 1000*60*60*24*7){
		        	//大于7天可以删除
		        	//System.out.println("f.getName() "+f.getName()+" "+name);
		        	if(f.getName().indexOf(name) !=-1){ 
		        		f.delete();
		        	}  
		        }
			}
		} 
	}
	@Override
	 public boolean isAsSevereAsThreshold(Priority priority) {
		
	  //只判断是否相等，而不判断优先级
	  return this.getThreshold().equals(priority);
	 }
}

