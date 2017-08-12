package com.hy.ren.rpc.consumer;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hy.ren.rpc.model.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Description:
 * 
 * @author Eddie.Wei
 * @version 1.0
 * @since Aug 9, 2017
 */
public class RpcConsumerHandler extends SimpleChannelInboundHandler<Response> {
	private static final Logger logger = LoggerFactory.getLogger(RpcConsumerHandler.class);
	private Response response;

	private String getRemoteAddress(ChannelHandlerContext ctx) {
		String address = ctx.channel().remoteAddress().toString();
		address = address.substring(1, address.length());
		return address;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("RpcConsumerHandler:channelActive channel is active to {}，reset count for reconnection to 0 ",
				getRemoteAddress(ctx));

		ctx.fireChannelActive();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
		this.response = response;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info("The current connection is inactive.");
		super.channelInactive(ctx);

		// TODO
		// MsgHandle.getInstance().channel = null;

		// reconnecting to server
		ctx.channel().eventLoop().schedule(new Runnable() {
			@Override
			public void run() {
				RpcConsumer.doConnect();
			}
		}, 10, TimeUnit.SECONDS);
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("RpcConsumerHandler:exceptionCaught,exception{}", cause.getMessage());
		ctx.close();
	}

	public Response getResponse() {
		return response;
	}
}
