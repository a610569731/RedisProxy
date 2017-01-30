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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.openlibin.net.kit.log.Logger;

 
/**
 * SelectorGroup manages Selector threads <code>SelectorThread</code>, round-robin distributing 
 * the socket channel encapsulated in <code>Session</code> to SelectorThread(NIO event engine)
 * 
 *
 */
public class SelectorGroup implements Closeable {
	public static final int DEFAULT_EXECUTOR_COUNT = 64;
	
	private static final Logger log = Logger.getLogger(SelectorGroup.class); 
	
	/* Shared thread pool for asynchronous work of the underlying network */
	private ExecutorService executor; //业务处理器
	private int selectorCount = defaultSelectorCount(); //default to #CPU/2
	private int executorCount = DEFAULT_EXECUTOR_COUNT; //default to 64
	private SelectorThread[] selectorThreads;
	private AtomicInteger selectorIndex = new AtomicInteger(0);
	private String name = "SelectorGroup";
	private String selectorNamePrefix = "Selector";
	
	private int excutorClientCount = 0;
	
	/**
	 * 
	 * public SelectorGroup selectorCount(int count){ 
		if(count <= 0){
			this.selectorCount = defaultSelectorCount();
		} else {
			this.selectorCount = count;
		}
		return this;
	}
	 * */
	
	public SelectorGroup excutorClientCount(int count){ 
		if(count <= 0){
			this.excutorClientCount = 1;
		} else {
			this.excutorClientCount = count;
		}
		return this;
	}
	protected volatile boolean started = false;  
	
	private Map<String, IoAdaptor> acceptIoAdaptors = new ConcurrentHashMap<String, IoAdaptor>();
 
	 private SessionReadThreadPool sessionReadThreadPool = null;
	
	 private List<ThreadPoolExecutor> listExecutor = null;
	AtomicInteger acCurrent = new AtomicInteger(-1);
	Map<Session, ThreadPoolExecutor> mapThread =  null;
	
	 
	public ExecutorService getExecutorService(Session session){
 			
			if(mapThread.containsKey(session)){
				return mapThread.get(session);
 			}else{

				int index = 0;//acCurrent.incrementAndGet();
				if(index < executorCount){
				}else{
					index = 0;
					acCurrent.set(0); 
				}
				ThreadPoolExecutor sessionReadThread =  listExecutor.get(index);
				return sessionReadThread;
 			} 
	} 
	private void init() throws IOException{
		if(excutorClientCount == 0){
			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(executorCount,
					executorCount, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()); 
	 		 this.executor = threadPoolExecutor;
			 
		}else{
			mapThread = new ConcurrentHashMap<>();
			sessionReadThreadPool = new SessionReadThreadPool(excutorClientCount,excutorClientCount);
		}
		this.selectorThreads = new SelectorThread[this.selectorCount];
		for(int i=0;i<this.selectorCount;i++){
			String selectorName = String.format("%s-%s-%d", name, selectorNamePrefix, i);
			this.selectorThreads[i] = new SelectorThread(this, selectorName);
		}
	}
	
	/**
	 * Get selector thread by index
	 * @param index index of the selector threads array managed by this dispatcher
	 * @return corresponding SelectorThread
	 */
	public SelectorThread getSelector(int index){
		if(index <0 || index>=this.selectorCount){
			throw new IllegalArgumentException("Selector index should >=0 and <"+this.selectorCount);
		}
		return this.selectorThreads[index];
	}
	
	/**
	 * round-robin balancing on all the selectors
	 * @return next available SelectorThread
	 */
	public SelectorThread nextSelector(){
		int nextIdx = this.selectorIndex.getAndIncrement()%this.selectorCount;
		if(nextIdx < 0){
			this.selectorIndex.set(0);
			nextIdx = 0;
		} 
		return this.selectorThreads[nextIdx];
	}

	/**
	 * Register a channel with interestOps
	 * @param channel SelectableChannel to register
	 * @param ops interest operations(READ/WRITE) to register on the underlying channel
	 * @throws IOException if register fails
	 */
	public void registerChannel(SelectableChannel channel, int ops) throws IOException{
		this.nextSelector().registerChannel(channel, ops);
	}
	
	/**
	 * Directly register a Session with internal channel register
	 * @param ops interest operations(READ/WRITE) to register on the underlying channel
	 * @param sess Session holding the channel
	 * @throws IOException if register fails
	 */
	public void registerSession(int ops, Session sess) throws IOException{
		if(sess.selectorGroup() != this){
			throw new IOException("Unmatched Dispatcher");
		}
		this.nextSelector().registerSession(ops, sess);
	}
	
	/**
	 * Get selector thread attaching the key specified
	 * @param key SelectionKey of a channel
	 * @return selector thread attaching the key
	 */
	public SelectorThread getSelector(SelectionKey key){
		for(SelectorThread e : this.selectorThreads){
			if(key.selector() == e.selector){
				return e;
			}
		}
		return null;
	}
	/**
	 * Start the dispatcher, which starts the underlying selector threads
	 */
	public  void start() {
		if (this.started) {
			return;
		}
		synchronized (this) { 
			try {
				this.init();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e); 
			}
			this.started = true;
			for (SelectorThread dispatcher : this.selectorThreads) {
				dispatcher.start();
			} 
			log.info("%s(SelectorCount=%d) started", this.name, this.selectorCount);
		}
		
	}
	
	/**
	 * Stop the dispatcher, interrupting the selector threads managed.
	 */
	public synchronized void stop() {
		if (!this.started)
			return; 
		
		for (SelectorThread selectorThread : this.selectorThreads) {
			selectorThread.interrupt();
		} 
		executor.shutdown();
		//this.started = false;
		log.info("%s(SelectorCount=%d) stopped", this.name, this.selectorCount);
		
	} 
	 
	/**
	 * Close this dispatcher, just call the stop
	 */
	public void close() throws IOException {
		this.stop();
	}
	
	public boolean isStarted(){
		return this.started;
	}

	
	public void removeAcceptIoAdaptor(SocketAddress address){
		acceptIoAdaptors.remove(""+address);
	}
	
	public IoAdaptor ioAdaptor(SocketAddress address){
		return acceptIoAdaptors.get(""+address);
	}
	
	public ExecutorService executorService(){
		return this.executor;
	}
	//线程池处理任务 
	public void asyncRun(Session session, Runnable task){
		//TODO
		//
		//task.run();
		///this.sessionReadThreadPool.put(session, task);
		//getExecutorService(session).submit(task);
		if(excutorClientCount == 0){
			this.executor.submit(task);
		}else{
			sessionReadThreadPool.put(session, task);
			//getExecutorService(session).submit(task);
		}
 		 
	}
	
	public int selectorCount(){
		return this.selectorCount;
	}
	
	public SelectorGroup selectorCount(int count){ 
		if(count <= 0){
			this.selectorCount = defaultSelectorCount();
		} else {
			this.selectorCount = count;
		}
		return this;
	}
	
	public static int defaultSelectorCount(){ 
		int c = Runtime.getRuntime().availableProcessors()/2;
		if(c <= 0) c = 1;
		return c;
	}
	
	public SelectorGroup executorCount(int count){ 
		if(count <= 0){
			this.executorCount = DEFAULT_EXECUTOR_COUNT;
		} else {
			this.executorCount = count;
		}
		return this;
	}
	
	public ExecutorService getExecutorService(){
		return this.executor;
	}
	
	
	public SelectorGroup name(String name){ 
		this.name = name;
		return this;
	}
	
	public ServerSocketChannel registerServerChannel(String address, IoAdaptor ioAdaptor) throws IOException{
		String[] blocks = address.split("[:]");
		if(blocks.length != 2){
			throw new IllegalArgumentException(address + " is invalid address");
		} 
		String host = blocks[0];
		int port = Integer.valueOf(blocks[1]);
		return registerServerChannel(host, port, ioAdaptor);
	}
	/**
	 * 开始注册
	 * @param host
	 * @param port
	 * @param ioAdaptor
	 * @return
	 * @throws IOException
	 */
	public ServerSocketChannel registerServerChannel(String host, int port, IoAdaptor ioAdaptor) throws IOException{
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    	serverSocketChannel.configureBlocking(false);
    	//serverSocketChannel.socket().setReuseAddress(true);
    	serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
    	
    	acceptIoAdaptors.put(""+serverSocketChannel.socket().getLocalSocketAddress(), ioAdaptor); 
    	this.registerChannel(serverSocketChannel, SelectionKey.OP_ACCEPT); 
    	return serverSocketChannel;
	}
	
	
	public void unregisterServerChannel(ServerSocketChannel channel) throws IOException{
		removeAcceptIoAdaptor(channel.socket().getLocalSocketAddress());
		channel.close();
	}
	
	public Session registerClientChannel(String address, IoAdaptor ioAdaptor ) throws IOException{
		
	
		String[] blocks = address.split("[:]");
		if(blocks.length != 2){
			throw new IllegalArgumentException(address + " is invalid address");
		} 
		String host = blocks[0];
		int port = Integer.valueOf(blocks[1]);
		return registerClientChannel(host, port, ioAdaptor);
	} 
	
	public Session registerClientChannel(String host, int port, IoAdaptor ioAdaptor ) throws IOException{
    	Session session = createClientSession(host, port, ioAdaptor);
    	this.registerSession(SelectionKey.OP_CONNECT, session);	
    	return session;
	} 
	
	public Session createClientSession(String address, IoAdaptor ioAdaptor ) throws IOException{
		String[] blocks = address.split("[:]");
		if(blocks.length != 2){
			throw new IllegalArgumentException(address + " is invalid address");
		} 
		String host = blocks[0];
		int port = Integer.valueOf(blocks[1]);
		return createClientSession(host, port, ioAdaptor);
	} 
	
	public Session createClientSession(String host, int port, IoAdaptor ioAdaptor ) throws IOException{
		SocketChannel channel = SocketChannel.open();
    	channel.configureBlocking(false);  
    	channel.socket().setReuseAddress(true);
    	channel.connect(new InetSocketAddress(host, port)); 
    	Session session = new Session(this, channel, ioAdaptor);
    	return session;
	}
}
