package com.hy.ren.rpc.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import com.hy.ren.rpc.common.PacketType;
import com.hy.ren.rpc.model.Request;
import com.hy.ren.rpc.model.Response;
import com.hy.ren.rpc.utils.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 * Description: The server-side handler, to handle RPC request.
 * 
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<Request> {
	private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
	private final Map<String, Object> handlerMap;
	private int counter; // missing count for heartbeat

	private void resetCounter() {
		counter = 0;
	}
	
	public RpcServerHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	private String getRemoteAddress(ChannelHandlerContext ctx) {
		String address = ctx.channel().remoteAddress().toString();
		address = address.substring(1, address.length());
		return address;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelActive with " + getRemoteAddress(ctx));
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("channelInactive with " + getRemoteAddress(ctx));
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
		switch (request.getPacketType()) {
		case PT_HEARTBEAT:
			handleHeartbreat(ctx, request);
			break;

		case PT_REQUEST:
			handleRequest(ctx, request);
			break;
		case PT_RESPONSE:
			handleResponse(ctx, request);
			break;
		default:
			break;
		}
	}

	private void handleResponse(ChannelHandlerContext ctx, Request request) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * Description: handle request package from client.
	 * 
	 * @param
	 * @return void
	 */
	private void handleRequest(ChannelHandlerContext ctx, Request request) {
		resetCounter();
		
		// Create and initialize RPC response object.
		Response response = new Response();
		response.setId(request.getId());
		response.setPacketType(PacketType.PT_RESPONSE);
		try {
			Object result = handle(request);
			response.setResult(result);
		} catch (Exception e) {
			logger.error("handle result failure", e);
			response.setException(e);
		}

		// Write RPC response object and auto close connection.
		ctx.writeAndFlush(response);
		logger.info(
				"Sent response to client(" + getRemoteAddress(ctx) + "), value is:" + response.getResult().toString());
		
		ReferenceCountUtil.release(request);

	}

	/**
	 * 
	 * Description: handle heart beat package from client.
	 * 
	 * @param
	 * @return void
	 */
	private void handleHeartbreat(ChannelHandlerContext ctx, Request request) {
		resetCounter();
		
		logger.info("got heart beat package from client: {}", ctx.channel().remoteAddress().toString());
		ReferenceCountUtil.release(request);
	}

	private Object handle(Request request) throws InvocationTargetException {
		// Get service object
		String serviceName = request.getInterfaceName();
		String serviceVersion = request.getServiceVersion();
		if (StringUtil.isNotEmpty(serviceVersion)) {
			serviceName += "-" + serviceVersion;
		}

		Object serviceBean = handlerMap.get(serviceName);
		if (serviceBean == null) {
			throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
		}

		// Get necessary parameters for reflection invoke.
		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();

		// Execute reflection invoke
		// Method method = serviceClass.getMethod(methodName, parameterTypes);
		// method.setAccessible(true);
		// return method.invoke(serviceBean, parameters);

		// Execute reflection invoke using CGLib
		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

		return serviceFastMethod.invoke(serviceBean, parameters);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("server caught exception", cause);
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			if (counter >= 3) {
				// will close connection if missed 3 heart beat.
				ctx.channel().close().sync();
				logger.warn("close the connection with client:{}", getRemoteAddress(ctx));
			} else {
				counter++;
				logger.warn("missed " + counter + " heart beat package from client: {}", getRemoteAddress(ctx));
			}
		}
	}
}
