package com.hy.ren.rpc.consumer;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hy.ren.rpc.codec.RpcDecoder;
import com.hy.ren.rpc.codec.RpcEncoder;
import com.hy.ren.rpc.model.Request;
import com.hy.ren.rpc.model.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Description: The client use it to send RPC request
 * 
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public class RpcConsumer {
	private static final Logger logger = LoggerFactory.getLogger(RpcConsumer.class);
	private static String host;
	private static int port;
	private Channel channel;
	private RpcConsumerHandler consumerHandler;
	private static ChannelFutureListener channelFutureListener = null;
	private static Bootstrap bootstrap;

	private final ClientIdleStateHandler clientIdleStateHandler = new ClientIdleStateHandler(); 
	
	public RpcConsumer(String serverAddr, int serverPort) {
		host = serverAddr;
		port = serverPort;
		consumerHandler = new RpcConsumerHandler();
	}

	public void init() throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();

		bootstrap = new Bootstrap();
		bootstrap.group(group);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel channel) throws Exception {
				ChannelPipeline pipeline = channel.pipeline();
				pipeline.addLast(new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS));
				pipeline.addLast(new RpcEncoder(Request.class));
				pipeline.addLast(new RpcDecoder(Response.class));
				pipeline.addLast(clientIdleStateHandler);
				pipeline.addLast(consumerHandler);
				
			}
		});

		// Set properties for TCP
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_TIMEOUT, 5000);

		channelFutureListener = new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					logger.info("Succeeded to reconnect to server!");

				} else {
					logger.warn("Failed to reconnect to server!");
					// Reconnect after 3 seconds.
					f.channel().eventLoop().schedule(new Runnable() {
						@Override
						public void run() {
							doConnect();
						}
					}, 3, TimeUnit.SECONDS);
				}
			}
		};
		
		doConnect();
	}

	/**
	 * Description: connection to server.
	 * @param 
	 * @return void
	 */
	public static void doConnect() {
		logger.info("Connecting to server......");
		ChannelFuture future = null;
		try {
			future = bootstrap.connect(new InetSocketAddress(host, port));
			future.addListener(channelFutureListener);

		} catch (Exception e) {
			e.printStackTrace();
			// future.addListener(channelFutureListener);
			logger.warn("Closed connection.");
		}

	}

	public Response send(Request request) {
		long time = System.currentTimeMillis();
		logger.info("Sent rpc request to server,id:{}", request.getId());

		try {
			channel.writeAndFlush(request).sync();
		} catch (InterruptedException e) {
			logger.error("RpcResponse:send,exception:{}", e.getMessage());
		}

		logger.info("Got rpc response from server,id:{},escaped time:{}ms",
				consumerHandler.getResponse().getId(), System.currentTimeMillis() - time);

		return (consumerHandler.getResponse());
	}
}