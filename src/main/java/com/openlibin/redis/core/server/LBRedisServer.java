package com.openlibin.redis.core.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openlibin.redis.core.protocol.RedisReplyEncoder;
import com.openlibin.redis.core.protocol.RedisRequestDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class LBRedisServer {
	private Logger logger = LoggerFactory.getLogger(LBRedisServer.class);

	

	// 线程组
	private static EventLoopGroup bossGroup = new NioEventLoopGroup(Runtime
			.getRuntime().availableProcessors());
	private static EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime
			.getRuntime().availableProcessors());
	/**
	 * 启动系统，开启接收连接，处理业务
	 */
	public void start() {  

		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
				.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch)
							throws Exception {
						ch.pipeline().addLast("RedisRequestDecoder",
								new RedisRequestDecoder());
						ch.pipeline().addLast("RedisReplyEncoder", 
								new RedisReplyEncoder());
						ch.pipeline().addLast(
								"FfanRedisServerHandler",
								new LBRedisServerHandler());
					}
				});
		ChannelFuture channelFuture = bootstrap.bind(
				"0.0.0.0",
				15589);
		channelFuture.syncUninterruptibly();
		System.out.println("RedisProxy_Server 已经启动 "+15589 +" "+Runtime
				.getRuntime().availableProcessors());
	}
}
