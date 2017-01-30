package com.openlibin.slice;

import com.openlibin.util.StringUtil;

public class Node {
	private String ip;
	private int port;
	private String password;
	public Node(){
		
	}

	public Node(String host){
		String arr[] =  host.split(":");
		if(arr.length>=2){
			ip = arr[0];
			port = StringUtil.parseInt(arr[1]);
			if(arr.length>=3){
				password = arr[2];
			}
		} 
	}
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return  ip+":"+port;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	 
	
}
