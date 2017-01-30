package com.openlibin.net.buffer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.openlibin.net.core.IoBuffer;
import com.openlibin.net.kit.log.Logger;
// import org.zsql.server.MysqlMessageAdaptor;
 
import java.util.concurrent.CopyOnWriteArrayList;
public class PooledBufferPool {
	 
	private static final Logger log = Logger.getLogger(PooledBufferPool.class); 

    /**
     * 用于写的bytebuffer的大小设置
     */
    private static int block = 1024*4;
    /**
     * bytebuffer池初始大小
     */
    private static int poolSize = 3000;
    /**
     * 已使用的bytebuffer数量
     */
    public  AtomicInteger usedCount = new AtomicInteger(0);
    
    /**
     * 可用
     */
    public  AtomicInteger usableCount = new AtomicInteger(0);

    private  ConcurrentLinkedQueue<IoBuffer> bufferQueue = new ConcurrentLinkedQueue<IoBuffer>();
    
    private  List<IoBuffer> useBufferQueue = new CopyOnWriteArrayList<IoBuffer>();

    public static PooledBufferPool DEFAULT = new PooledBufferPool(poolSize,1024*4);
 
    private PooledBufferPool(int poolSize,int block) {
        for (int i = 0; i < poolSize; i++) {
        	IoBuffer byteBuffer = IoBuffer.allocate(block);
            bufferQueue.offer(byteBuffer);
            usableCount.incrementAndGet();
 
        }
    }
    public void recycle(IoBuffer byteBuffer){
        int r = usableCount.incrementAndGet();

    	byteBuffer.flip();
    	byteBuffer.clear();
		usedCount.decrementAndGet();
        useBufferQueue.remove(byteBuffer);
        byteBuffer.setRefCnt(1);
    	//log.info("recycle 剩余:"+r);

        bufferQueue.offer(byteBuffer);

    }
 
 
    public  IoBuffer getBuffer() {
    	
    	IoBuffer byteBuffer = null;
    	
    	
    	if(usableCount.get() > 0){
    		 byteBuffer = bufferQueue.poll();
    		 
            if (byteBuffer != null ) {
              //  byteBuffer.retain();
                int r = usableCount.decrementAndGet();
            	//log.info("getBuffer 剩余:"+r);
            } else {
             	log.info("缓冲池已经用尽，请考虑增加缓冲区配置！");
            	byteBuffer =  IoBuffer.allocate(block);
            }
    	} else {
        	//log.info("缓冲池已经用尽，请考虑增加缓冲区配置！！！");
        	byteBuffer =  IoBuffer.allocate(block);
        } 
    	 
        usedCount.incrementAndGet();
        
        useBufferQueue.add(byteBuffer);
        return byteBuffer;

    }

    public static void main(String[] args) {
		IoBuffer ioBuffer =   IoBuffer.allocate(1111);
		ioBuffer.retain();
		System.out.println(""+ioBuffer.limit()+","+ioBuffer.position()+" "+ioBuffer.buf.capacity()+" ");
		System.out.println(ioBuffer.refCnt());

		ioBuffer.release(1);
		System.out.println(ioBuffer.refCnt());
		
		//ioBuffer.release();

		/*System.out.println(ioBuffer.hashCode());

		IoBuffer ioBuffer2 = DEFAULT.getBuffer();
		ioBuffer2.release();
		System.out.println(ioBuffer2.hashCode());
		

		IoBuffer ioBuffer3 = DEFAULT.getBuffer();
		ioBuffer3.release();

		System.out.println(ioBuffer3.hashCode());*/
	}
}