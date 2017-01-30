package com.openlibin.net.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.openlibin.net.kit.log.Logger;
  

public class WriteBufferPool {
	 
	private static final Logger log = Logger.getLogger(WriteBufferPool.class); 

    /**
     * 用于写的bytebuffer的大小设置
     */
    private static int block = 1024*4;
    /**
     * bytebuffer池初始大小
     */
    private static int poolSize = 10;
    /**
     * 已使用的bytebuffer数量w
     */
    public static AtomicInteger usedCount = new AtomicInteger(0);
    private static ConcurrentLinkedQueue<ByteBuffer> bufferQueue = new ConcurrentLinkedQueue<ByteBuffer>();
    private static WriteBufferPool writeBufferPool = new WriteBufferPool(10,1024*4);
 
    private WriteBufferPool(int poolSize,int block) {
        for (int i = 0; i < poolSize; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(block);
            bufferQueue.offer(byteBuffer);
 
        }
    }
 
 
    public static ByteBuffer getBuffer() {
 
        ByteBuffer byteBuffer = bufferQueue.poll();
 
        if (byteBuffer != null) {
            usedCount.incrementAndGet();
            return byteBuffer;
        } else {
        	log.info("缓冲池已经用尽，请考虑增加缓冲区配置！");
            return ByteBuffer.allocate(block);
        }
 
    }
 
    public static void freeBuffer(ByteBuffer byteBuffer) {

        bufferQueue.offer(byteBuffer);
        usedCount.decrementAndGet();
 
    }
}