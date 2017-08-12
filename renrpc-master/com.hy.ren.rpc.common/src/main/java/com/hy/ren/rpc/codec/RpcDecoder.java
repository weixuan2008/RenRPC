package com.hy.ren.rpc.codec;

import java.util.List;

import com.hy.ren.rpc.utils.SerializationUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 
 * Description: Decoder for RPC message.
 *
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public class RpcDecoder extends ByteToMessageDecoder {
	private Class<?> genericClass;

	public RpcDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> msg) throws Exception {
		if (in.readableBytes() < 4)
			return;

		in.markReaderIndex();
		int dataLength = in.readInt();
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}

		byte[] data = new byte[dataLength];
		in.readBytes(data);
		msg.add(SerializationUtil.deserialize(data, genericClass));
	}
}
