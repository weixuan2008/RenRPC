package com.hy.tech.points003.netty12.comm.longconnection.client;


import java.util.concurrent.TimeUnit;

import com.hy.tech.points003.netty07.comm.decode.common.MarshallingCodeCFactory;
import com.hy.tech.points003.netty07.comm.decode.common.Request;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class Client {
	private EventLoopGroup group;
	private Bootstrap b;
	private ChannelFuture cf;

	// 单例
	private static class SingletonHolder {
		static final Client instance = new Client();
	}

	public static Client getInstance() {
		return SingletonHolder.instance;
	}

	private Client() {
		group = new NioEventLoopGroup();
		b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel sc) throws Exception {
						sc.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
						sc.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
						// 超时handler（当服务器端与客户端在指定时间以上没有任何进行通信，则会关闭通道）
						sc.pipeline().addLast(new ReadTimeoutHandler(5)); // 时限5s, 读服务端超时没数据则断开
						sc.pipeline().addLast(new ClientHandler());
					}
				});
	}

	public void connect() {
		try {
			this.cf = b.connect("127.0.0.1", 8765).sync();
			System.out.println("远程服务器已经连接, 可以进行数据交换");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ChannelFuture getChannelFuture() {
		if (this.cf == null) { // 初次连接
			this.connect();
		}
		if (!this.cf.channel().isActive()) { // 重连
			this.connect();
		}
		return this.cf;
	}

	public static void main(String[] args) throws Exception {
		final Client c = Client.getInstance();

		ChannelFuture cf = c.getChannelFuture();
		for (int i = 1; i <= 3; i++) {
			Request request = new Request();
			request.setId("" + i);
			request.setName("request" + i);
			request.setRequestMessage("数据信息" + i);
			cf.channel().writeAndFlush(request);
			TimeUnit.SECONDS.sleep(4); // 间隔4s发送一次数据
		}

		cf.channel().closeFuture().sync(); // 阻塞至超时关闭

		// 这里用子线程重连并发送数据一次
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("进入子线程重连一次");
					ChannelFuture cf = c.getChannelFuture();
					assert true == cf.channel().isActive(); // 断言
					// 再次发送数据
					Request request = new Request();
					request.setId("" + 4);
					request.setName("request" + 4);
					request.setRequestMessage("数据信息" + 4);
					cf.channel().writeAndFlush(request);
					cf.channel().closeFuture().sync();
					System.out.println("子线程完成");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		System.out.println("断开连接,主线程结束..");
	}

}
