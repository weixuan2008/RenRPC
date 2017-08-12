package com.hy.tech.points003.netty22.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SimpleChatClient {
	private static final Logger logger = LoggerFactory.getLogger(SimpleChatClient.class);

	private final String host;
	private final int port;

	public SimpleChatClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public static void main(String[] args) throws InterruptedException {
		new SimpleChatClient("192.168.56.1", 9000).run();
	}

	private void run() {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class).handler(new SimpleChatClientInitializer());

			Channel channel = bootstrap.connect(host, port).sync().channel();
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			while (true) {
				channel.writeAndFlush(in.readLine() + "\r\n");
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		} finally {
			group.shutdownGracefully();
		}

	}
}
