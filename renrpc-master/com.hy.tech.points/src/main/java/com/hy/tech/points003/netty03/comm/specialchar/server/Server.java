package com.hy.tech.points003.netty03.comm.specialchar.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Server {
	public static void main(String[] args) throws Exception {
		EventLoopGroup pGroup = new NioEventLoopGroup();
		EventLoopGroup cGroup = new NioEventLoopGroup();

		ServerBootstrap b = new ServerBootstrap();
		b.group(pGroup, cGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
				.option(ChannelOption.SO_SNDBUF, 32 * 1024).option(ChannelOption.SO_RCVBUF, 32 * 1024)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel sc) throws Exception {
						// 使用DelimiterBasedFrameDecoder设置结尾分隔符$_
						ByteBuf buf = Unpooled.copiedBuffer("$_".getBytes());
						sc.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, buf));
						// 设置字符串形式的解码. 经过StringDecoder, Handler回调方法中接收的msg的具体类型就是String了(不再是ByteBuffer).
						// 但写时仍需要传入ByteBuffer
						sc.pipeline().addLast(new StringDecoder());
						// 通信数据的处理逻辑
						sc.pipeline().addLast(new ServerHandler());
					}
				});
		// 4 绑定连接
		ChannelFuture cf = b.bind(8765).sync();

		// 等待服务器监听端口关闭
		cf.channel().closeFuture().sync();
		pGroup.shutdownGracefully();
		cGroup.shutdownGracefully();
	}
}
