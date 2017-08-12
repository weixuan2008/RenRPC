package com.hy.tech.points003.netty18.http.helloworld.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

public class HttpHelloWorldServerHandler extends ChannelInboundHandlerAdapter {
	private static final byte[] CONTENT = "HELLO WORLD".getBytes();

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(" server channel active... ");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) msg;
			if (HttpUtil.is100ContinueExpected(req)) {
				ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
			}
			boolean keepAlive = HttpUtil.isKeepAlive(req);
			// 构造响应
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(CONTENT));
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

			if (!keepAlive) {
				// Request短连接, 写完后直接关闭
				ctx.write(response).addListener(ChannelFutureListener.CLOSE);
			} else {
				// 长连接, response也设置为KEEP_ALIVE
				response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				ctx.write(response);
			}
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) throws Exception {
		t.printStackTrace();
		ctx.close();
	}

}
