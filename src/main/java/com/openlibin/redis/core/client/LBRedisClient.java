package com.openlibin.redis.core.client; 
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.openlibin.redis.core.command.impl.RedisCommand;
import com.openlibin.redis.core.connection.IConnectionCallBack;
import com.openlibin.redis.core.enums.ChannelState;
import com.openlibin.redis.core.protocol.RedisReplyDecoder;
import com.openlibin.redis.core.protocol.RedisRequestEncoder;
import com.openlibin.redis.util.RequestIdGeneratorUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class LBRedisClient {
	Bootstrap bootstrap = null;
	private String ip;
	private int port;
	private String password;
	private LBRedisClientPool lbRedisClientPool;
	
	
	public LBRedisClient(String ip, int port,String password){
		this.ip = ip;
		this.port = port;
		this.password = password;
		initClientBootstrap();
	}
	private void initClientBootstrap() {
        bootstrap = new Bootstrap(); 
        
        NioEventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new  ChannelInitializer<SocketChannel>() {

			@Override 
			protected void initChannel(SocketChannel ch) throws Exception {
				LBRedisClientInHandler lbredisClientInHandler = new LBRedisClientInHandler(LBRedisClient.this);
				lbredisClientInHandler.setPassword(password);
				ch.pipeline().addLast("RedisReplyDecoder",new RedisReplyDecoder());
				ch.pipeline().addLast("RedisRequestEncoder",new RedisRequestEncoder());
				ch.pipeline().addLast("ClientInHandler",lbredisClientInHandler);
				ch.pipeline().addLast("ClientOutHandler",new LBRedisClientOutHandler());
			} 
        });

        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

		/* 实际上，极端情况下，connectTimeout会达到500ms，因为netty nio的实现中，是依赖BossThread来控制超时，
         如果为了严格意义的timeout，那么需要应用端进行控制。
		 */
        int timeout = 3000;
        if (timeout <= 0) {
        }
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
    }
	 
	public boolean isClosed() {
        return state.isCloseState();
	}

	public boolean isAvailable() {
        return state.isAliveState();
	}
	Channel  channel ;
	ChannelState state =ChannelState.UNINIT ;
	//打开连接
	public boolean open() {
		if (isAvailable()) {
			return true;
		}
		try {
			 System.out.println("open  ");
			ChannelFuture channelFuture = bootstrap.connect(
					new InetSocketAddress(ip, port));

 
			// 不去依赖于connectTimeout
			boolean result = channelFuture.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);
            boolean success = channelFuture.isSuccess();

			if (result && success) {
				channel = channelFuture.channel();
				state = ChannelState.ALIVE;

				return true;
			} 
            boolean connected = false;
            if(channelFuture.channel() != null){
                connected = channelFuture.channel().isOpen();
            }  
			if (channelFuture.cause() != null) {
				channelFuture.cancel(true);
			} else {
				channelFuture.cancel(true);
            }
			return connected;
		}  catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (!state.isAliveState()) {
				 incrErrorCount(); 
			}
		}
		return false;  
	}

	public void close() {
		close(0);
	}

	public void close(int timeout) {
		try {
			if (channel != null) {
				channel.close();
			}
			state = ChannelState.CLOSE;
		} catch (Exception e) {
			e.printStackTrace();
 		}
	}
	public void write(RedisCommand request,IConnectionCallBack connectionCallBack) {
		try{
        	 
            if(!this.isAvailable() ||  this.channel  == null){
            	this.open();
            	System.out.println("lbRedisClient is open");
            	return ;
            }     
    		Long key=RequestIdGeneratorUtils.getRequestId();  
    		LBRedisClientInHandler.requestThreadMap.put(Thread.currentThread().getId(), key);
    		LBRedisClientInHandler.callBackMap.put(key, connectionCallBack);
    		
    		request.setRequestKey(key);  
            ///this.channel.attr(LBRedisClientOutHandler.CALLBACK_KEY).set(connectionCallBack);
    		this.channel.writeAndFlush(request);    
    		
		}catch(Exception e){  
			e.printStackTrace();
			open();
		}finally{
		} 
	}
	
	
	
	AtomicLong errorCount = new AtomicLong();
	int  maxClientConnection = 100; 
	 public void incrErrorCount() {
        long count = errorCount.incrementAndGet();

        // 如果节点是可用状态，同时当前连续失败的次数超过限制maxClientConnection次，那么把该节点标示为不可用
        if (count >= maxClientConnection && state.isAliveState()) {
            synchronized (this) {
                count = errorCount.longValue();

                if (count >= maxClientConnection && state.isAliveState()) {
                //    logger.error("NettyClient unavailable Error: url=" + redisProxyURL);
                    state = ChannelState.UNALIVE;
                }
            }
        }
	}
	 
    public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	
	
	public LBRedisClientPool getLbRedisClientPool() {
		return lbRedisClientPool;
	}
	public void setLbRedisClientPool(LBRedisClientPool lbRedisClientPool) {
		this.lbRedisClientPool = lbRedisClientPool;
	}
	public static void main(String[] args) {
		LBRedisClient lbRedisClient = new LBRedisClient("127.0.0.1", 6379, "123456");
		lbRedisClient.open();
	}
	
	
}
