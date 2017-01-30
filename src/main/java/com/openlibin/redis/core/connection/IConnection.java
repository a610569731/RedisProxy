/**
 * 
 */
package com.openlibin.redis.core.connection;

import com.openlibin.redis.core.command.impl.RedisCommand;

/**
 * @author liubing
 *
 */
public interface IConnection extends Pool{
	
	public void write(RedisCommand request,IConnectionCallBack connectionCallBack);
	
	 /**
     * open the channel
     * 
     * @return
     */
    boolean open();

    /**
     * close the channel.
     */
    void close();

    /**
     * close the channel gracefully.
     */
    void close(int timeout);

    /**
     * is closed.
     * 
     * @return closed
     */
    boolean isClosed();

    /**
     * the node available status
     * 
     * @return
     */
    boolean isAvailable();
}
