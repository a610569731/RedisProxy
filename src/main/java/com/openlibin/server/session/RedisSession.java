package com.openlibin.server.session;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.openlibin.net.core.Session;
import com.openlibin.redis.core.reply.IRedisReply;
public class RedisSession {
	private Session session;
	private int state;
	
	private byte[] mysqlSeed;
	private byte[] mysqlRestOfScrambleBuff;
	
	//登录的用户名
	private String user;
	private String useDb;
	private String host;
	private long processId;

	
	//0 不自动提交 1自动提交
	private AtomicInteger autocommit = new AtomicInteger(1);
	 
	public RedisSession(Session session){
		this.session = session;
		processId = System.currentTimeMillis() ;// IdWorkMap.get(MysqlSession.class);
	}
	
	public Session getSession() {
		return session;
	}
	
	public AtomicInteger getAutocommit() {
		return autocommit;
	}

	public void setAutocommit(AtomicInteger autocommit) {
		this.autocommit = autocommit;
	}
	   
	public void writeError(String error){
		/*
		ErrorPacket errorPacket = new ErrorPacket();
		errorPacket.message = error.getBytes();
		MysqlServerMessage messageResponse =  errorPacket.getPacketMessage();
		messageResponse.pakgId = ((byte)(1));
		write(messageResponse);
		*/
	}
	public void write(IRedisReply iRedisReply){
		if(session != null && session.isActive()){
			try {
				session.write(iRedisReply);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

	public byte[] getMysqlSeed() {
		return mysqlSeed;
	}

	public void setMysqlSeed(byte[] mysqlSeed) {
		this.mysqlSeed = mysqlSeed;
	}

	public byte[] getMysqlRestOfScrambleBuff() {
		return mysqlRestOfScrambleBuff;
	}

	public void setMysqlRestOfScrambleBuff(byte[] mysqlRestOfScrambleBuff) {
		this.mysqlRestOfScrambleBuff = mysqlRestOfScrambleBuff;
	}

	public String getUser() {
		return user;
	}
 
	public String getUseDb() {
		return useDb;
	}
 
	public long getProcessId() {
		return processId;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
 
	
	
}
