package com.hy.ren.rpc.consumer;

import java.util.UUID;

import com.hy.ren.rpc.common.PacketType;
import com.hy.ren.rpc.model.Request;
import com.hy.ren.rpc.utils.HashUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

@Sharable
public class ClientIdleStateHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent state = (IdleStateEvent) evt;
			switch (state.state()) {
			case READER_IDLE:
			case WRITER_IDLE:
			case ALL_IDLE:
				sendHeartbeatPacket(ctx);// Anyway, send heart beat message to server.
				break;

			default:
				super.userEventTriggered(ctx, evt);
			}
		}
	}

	private void sendHeartbeatPacket(ChannelHandlerContext ctx) {
		Request request = new Request();
		request.setId(HashUtil.bytes2Hex(HashUtil.md5(UUID.randomUUID().toString())));
        request.setPacketType(PacketType.PT_HEARTBEAT);
        ctx.writeAndFlush(request);
	}
}