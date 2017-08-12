package com.hy.ren.rpc.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.hy.ren.rpc.codec.RpcDecoder;
import com.hy.ren.rpc.codec.RpcEncoder;
import com.hy.ren.rpc.common.RpcService;
import com.hy.ren.rpc.model.Request;
import com.hy.ren.rpc.model.Response;
import com.hy.ren.rpc.registry.ServiceRegistry;
import com.hy.ren.rpc.utils.StringUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Description: To publish RPC service
 * 
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public class RpcProvider implements ApplicationContextAware, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(RpcProvider.class);
	private String serviceAddress;
	private ServiceRegistry serviceRegistry;
	
	// Save map relationship between service name and service object.
	private Map<String, Object> handlerMap = new HashMap<>();

	public RpcProvider(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public RpcProvider(String serviceAddress, ServiceRegistry serviceRegistry) {
		this.serviceAddress = serviceAddress;
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("Calling RpcServer.afterPropertiesSet ");

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			// Create and initialize bootstrap object for netty server-side
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("ping-pang", new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
					pipeline.addLast(new RpcDecoder(Request.class)); // decode rpc request
					pipeline.addLast(new RpcEncoder(Response.class));// encode rpc response
					pipeline.addLast(new RpcServerHandler(handlerMap)); // handle rpc request
				}
			});

			bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

			// Get ip address and port for rpc server.
			String[] addressArray = StringUtil.split(serviceAddress, ":");
			String ip = addressArray[0];
			int port = Integer.parseInt(addressArray[1]);

			// start rpc server
			ChannelFuture future = bootstrap.bind(ip, port).sync();

			// registry rpc server address
			if (serviceRegistry != null) {
				for (String interfaceName : handlerMap.keySet()) {
					serviceRegistry.register(interfaceName, serviceAddress);
					logger.info("register service: {} => {}", interfaceName, serviceAddress);
				}
			}
			logger.info("Server started on port {}", port);

			// close rpc server.
			future.channel().closeFuture().sync();

		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		logger.info("Calling RpcServer.setApplicationContext.");

		// Scan all classes with RpcServer annotation, and initialize handlerMap object.
		Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
		if (MapUtils.isNotEmpty(serviceBeanMap)) {
			for (Object serviceBean : serviceBeanMap.values()) {
				RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
				String serviceName = rpcService.value().getName();
				String serviceVersion = rpcService.version();
				if (StringUtil.isNotEmpty(serviceVersion)) {
					serviceName += "-" + serviceVersion;
				}

				handlerMap.put(serviceName, serviceBean);
			}
		}

	}

}