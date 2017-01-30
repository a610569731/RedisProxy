/**
 * The MIT License (MIT)
 * Copyright (c) 2009-2015 HONG LEIMING
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.openlibin.net.core;


import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.openlibin.net.buffer.AbstractReferenceCounted;
import com.openlibin.net.buffer.ReferenceCounted;
/** 
 * IoAdaptor is the core configurable entry that a networking application should be focus on, 
 * it personalizes the codec between object on the wire and business domain message object, and all
 * the other life cycle events of networking such as session accepted, connected, registered, on error
 * and etc. It is pretty simple compared to netty channel chain design.
 * 
 *
 */
public abstract class IoAdaptor   implements Codec { 
	
	private Codec codec; //pluggable codec
	
	public IoAdaptor codec(Codec codec){
		this.codec = codec;
		return this;
	}

	@Override
	public Object decode(IoBuffer buff,Session session) {
		return codec.decode(buff,session); 
	}
	
	@Override
	public IoBuffer encode(Object msg) { 
		return codec.encode(msg);
	}
	
	/**
	 * Server side event when server socket accept an incoming client socket.
	 * Session is still unregister during this stage, so the default action is to register it,
	 * typically with OP_READ interested, application can change this behavior for special usage.
	 * 
	 * This event is for server only
	 * 
	 * @param sess Session generated after accept of server channel
	 * @throws IOException if fails
	 */
	protected void onSessionAccepted(Session sess) throws IOException { 
		sess.selectorGroup().registerSession(SelectionKey.OP_READ, sess); 
	}
	/**
	 * Triggered after session registered, omit this event by default
	 * 
	 * @param sess session registered
	 * @throws IOException if fails
	 */
	protected void onSessionRegistered(Session sess) throws IOException {  
	
	} 
	/**
	 * Triggered after initiative client connection(session) is successful
	 * 
	 * This event is for client only
	 * 
	 * @param sess connected session
	 * @throws IOException if fails
	 */
	protected void onSessionConnected(Session sess) throws IOException{ 
		sess.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
	}
	
	/**
	 * Triggered before session is going to be destroyed, session is still legal in this stage
	 * 
	 * @param sess session to be destroyed
	 * @throws IOException if fails
	 */
	protected void onSessionToDestroy(Session sess) throws IOException{
		
	}
	
	/**
	 * Triggered after the session is destroyed
	 * 
	 * @param sess session destroyed, illegal in this stage
	 * @throws IOException if fails
	 */
	protected void onSessionDestroyed(Session sess) throws IOException{
		
	}
	/**
	 * Triggered when application level messaged is fully parsed(well framed).
	 * 
	 * @param msg application level message decided by the codec
	 * @param sess message generating session
	 * @throws IOException if fails
	 */
	protected void onMessage(Object msg, Session sess) throws IOException{
		
	}
	/**
	 * Triggered when session error caught
	 * @param e error ongoing
	 * @param sess corresponding session
	 * @throws IOException if fails
	 */
	protected void onException(Throwable e, Session sess) throws IOException{
		if(e instanceof IOException){
			throw (IOException) e;
		} else if (e instanceof RuntimeException){
			throw (RuntimeException)e;
		} else {
			throw new RuntimeException(e.getMessage(), e); //rethrow by default
		}
	}
}
