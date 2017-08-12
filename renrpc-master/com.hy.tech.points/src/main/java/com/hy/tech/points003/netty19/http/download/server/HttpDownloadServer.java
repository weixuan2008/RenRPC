package com.hy.tech.points003.netty19.http.download.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpDownloadServer {
	private static final String DEFAULT_URL = "/sources/";

	public void run(final int port, final String url) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// addLast的第一项为key, 自定义的
							// request解码器
							ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
							// response的编码器
							ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
							// chunked, 传输文件时分多个response分解地传输文件
							ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
							// ObjectAggregator, 将多个response合并为一个FullHttpResponse
							ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
							// 自定义业务逻辑handler
							ch.pipeline().addLast("fileServerHandler", new HttpDownoadServerHandler(url));
						}
					});
			ChannelFuture future = b.bind("127.0.0.1", port).sync();
			System.out.println("HTTP文件目录服务器启动，网址是 : " + "http://localhost:" + port + url);
			future.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8765;
		new HttpDownloadServer().run(port, DEFAULT_URL);
	}
}
