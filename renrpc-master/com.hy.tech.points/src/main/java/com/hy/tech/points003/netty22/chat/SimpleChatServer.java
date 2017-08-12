package com.hy.tech.points003.netty22.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class SimpleChatServer {
	private static final Logger logger = LoggerFactory.getLogger(SimpleChatServer.class);

	private int port;

	public SimpleChatServer(int port) {
		this.port = port;
	}

	public void run() throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(boss, worker)
			.channel(NioServerSocketChannel.class)
			.childHandler(new SimpleChatServerInitializer())
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);

			logger.info("SimpleChatServer started.");

			ChannelFuture f = bootstrap.bind(port).sync();

			f.channel().closeFuture().sync();

		} finally {
			worker.shutdownGracefully();
			boss.shutdownGracefully();

			logger.info("SimpleChatServer closed.");
		}

	}

	public static void main(String[] args) throws InterruptedException {
		int port = 9000;
		new SimpleChatServer(port).run();
	}
}
