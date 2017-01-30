package com.openlibin.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.openlibin.client.pool.ChannelManager;
import com.openlibin.client.pool.ChannelPool;
import com.openlibin.client.pool.RedisClient;
import com.openlibin.net.Client;
import com.openlibin.net.kit.log.Logger;
import com.openlibin.slice.Node;
import com.openlibin.util.TimeUtil;

public class ScheduledExecutor {
	private static final Logger log = Logger.getLogger(ScheduledExecutor.class);

	
	static ScheduledExecutor scheduledExecutor = null;
	public static ScheduledExecutor getInstance(){
		if(scheduledExecutor == null){
			scheduledExecutor = new ScheduledExecutor();
		}
		return scheduledExecutor;
	}
	List<Client> heartList = new  CopyOnWriteArrayList<Client>();
	
	public int heartbeatInterval = 60000;
	public int checkPoolInterval = 60000;

    public static final String NAME = "redis";
 
    private static final long TIME_UPDATE_PERIOD = 20L;

    private final Timer timer;


    // 系统时间定时更新任务
    private TimerTask updateTime() {
        return new TimerTask() {
            @Override
            public void run() {
                TimeUtil.update();
            }
        };
    }

    
	public ScheduledExecutor(){
		 
		
        this.timer = new Timer(NAME + "Timer", true);
        
        timer.schedule(updateTime(), 0L, TIME_UPDATE_PERIOD);

        timer.schedule(heartbeat(), 0L, heartbeatInterval);
        timer.schedule(checkPool(), 0L, checkPoolInterval);


	}
	public void putClient(Client  client){
		heartList.add(client);
	}
	
	// 心跳检查
    private TimerTask heartbeat() {
        return new TimerTask() {
            @Override
            public void run() {
        		for(Client client:heartList){
         			client.heartbeat();
        		}
            }
        };
    }
 	//连接池检查
    private TimerTask checkPool() {
        return new TimerTask() {
            @Override
            public void run() {
            	Map<Node, ChannelPool> map = ChannelManager.getInstacnce().getNodePoolMap();
            	 
            	 Iterator<Node> itNode = map.keySet().iterator();
            	 while(itNode.hasNext()){
            		 Node node = itNode.next();
            		 ChannelPool channelPool = map.get(node);
            		 //
            		 channelPool.check();
            	 }
            	
            }
        };
    }
	 
}
