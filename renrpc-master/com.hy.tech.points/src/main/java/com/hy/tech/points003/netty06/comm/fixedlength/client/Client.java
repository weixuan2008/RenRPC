package com.hy.tech.points003.netty06.comm.fixedlength.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Client {
	public static void main(String[] args) throws Exception {

		EventLoopGroup group = new NioEventLoopGroup();

		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				sc.pipeline().addLast(new FixedLengthFrameDecoder(5));
				sc.pipeline().addLast(new StringDecoder());
				sc.pipeline().addLast(new ClientHandler());
			}
		});

		ChannelFuture cf = b.connect("127.0.0.1", 8765).sync();

		cf.channel().writeAndFlush(Unpooled.wrappedBuffer("aaa".getBytes()));
		cf.channel().writeAndFlush(Unpooled.copiedBuffer("bbccccc".getBytes()));

		cf.channel().closeFuture().sync();
		group.shutdownGracefully();
	}
}
