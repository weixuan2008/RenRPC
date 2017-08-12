package com.hy.tech.points003.netty19.http.download.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

//注意这里继承了SimpleChannelInboundHandler<T>, 含泛型, 即指定了传入参数msg的类型
public class HttpDownoadServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private final String url;

	public HttpDownoadServerHandler(String url) {
		this.url = url;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		// 是否能理解(解码)请求
		if (!request.decoderResult().isSuccess()) {
			// 400
			sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		// 对请求的方法进行判断：如果不是GET方法则返回异常
		if (request.method() != HttpMethod.GET) {
			// 405
			sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
			return;
		}
		// 获取请求uri路径
		final String uri = request.uri();
		// 对url进行分析，返回本地路径
		final String path = parseURI(uri);
		// 如果 路径构造不合法，则path为null
		if (path == null) {
			// 403
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
		}

		// 创建file对象
		File file = new File(path);
		// 文件隐藏或不存在
		if (file.isHidden() || !file.exists()) {
			// 404
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		// 是文件夹
		if (file.isDirectory()) {
			if (uri.endsWith("/")) {
				// 如果以正常"/"结束 说明是访问的一个文件目录：则进行展示文件列表
				sendListing(ctx, file);
			} else {
				// 如果非"/"结束 则重定向，让客户端补全"/"并再次请求
				sendRedirect(ctx, uri + '/');
			}
			return;
		}
		// 如果所创建的file对象不是文件类型
		if (!file.isFile()) {
			// 403
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
		}

		// 随机文件读写对象
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");// 以只读的方式打开文件
		} catch (FileNotFoundException fnfe) {
			// 404
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}

		// 获取文件长度
		long fileLength = randomAccessFile.length();
		// 建立响应对象
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		// 设置响应信息
		HttpUtil.setContentLength(response, fileLength);
		// 设置Content-Type
		setContentTypeHeader(response, file);
		// 设置为KeepAlive
		if (HttpUtil.isKeepAlive(request)) {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		// 输出response header, HttpObjectAggregator能将其与下面输出整合合并
		ctx.write(response);

		// 写出ChunkedFile. 创建ChunkedFile需要使用RandomAccessFile并设置分段. 这里每次传输8192个字节
		ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192),
				ctx.newProgressivePromise());
		// 添加传输监听
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			@Override
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
				if (total < 0) {
					System.err.println("Transfer progress: " + progress);
				} else {
					System.err.println("Transfer progress: " + progress + " / " + total);
				}
			}

			@Override
			public void operationComplete(ChannelProgressiveFuture future) throws Exception {
				System.out.println("Transfer complete.");
			}
		});

		// 使用Chunked, 完成时需要发送标记结束的空消息体!
		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		// 如果当前连接请求非Keep-Alive, 最后一包消息发送完后, 服务器主动关闭连接
		if (!HttpUtil.isKeepAlive(request)) {
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (ctx.channel().isActive()) {
			// 500
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			ctx.close();
		}
	}

	// 判断非法URI的正则
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	private String parseURI(String uri) {
		try {
			// 使用UTF-8字符集
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				// 尝试ISO-8859-1
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				// 抛出预想外异常信息
				throw new Error();
			}
		}
		// 对uri进行细粒度判断：4步验证操作
		// step 1 基础验证
		if (!uri.startsWith(url)) {
			return null;
		}
		// step 2 基础验证
		if (!uri.startsWith("/")) {
			return null;
		}
		// step 3 将文件分隔符替换为本地操作系统的文件路径分隔符
		uri = uri.replace('/', File.separatorChar);
		// step 4 验证路径合法性
		if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.startsWith(".")
				|| uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
			return null;
		}
		// 利用当前工程所在目录 + URI相对路径 构造绝对路径
		return System.getProperty("user.dir") + File.separator + uri;
	}

	// 用正则表达式过滤文件名
	private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

	// 文件列表, 拼html文件
	private static void sendListing(ChannelHandlerContext ctx, File dir) {
		// 设置响应对象
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		// 响应头
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		// 构造文本内容
		StringBuilder ret = new StringBuilder();
		String dirPath = dir.getPath();
		ret.append("<!DOCTYPE html>\r\n");
		ret.append("<html><head><title>");
		ret.append(dirPath);
		ret.append(" 目录：");
		ret.append("</title></head><body>\r\n");
		ret.append("<h3>");
		ret.append(dirPath).append(" 目录：");
		ret.append("</h3>\r\n");
		ret.append("<ul>");
		ret.append("<li>链接：<a href=\"../\">..</a></li>\r\n");

		// 遍历文件, 生成超链接
		for (File f : dir.listFiles()) {
			// step 1: 跳过隐藏文件和不可读文件
			if (f.isHidden() || !f.canRead()) {
				continue;
			}
			String name = f.getName();
			// step 2: 跳过正则过滤的文件名
			if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
				continue;
			}
			ret.append("<li>链接：<a href=\"");
			ret.append(name);
			ret.append("\">");
			ret.append(name);
			ret.append("</a></li>\r\n");
		}
		ret.append("</ul></body></html>\r\n");
		// 构造ByteBuf，写入缓冲区
		ByteBuf buffer = Unpooled.copiedBuffer(ret, CharsetUtil.UTF_8);
		// 进行写出操作
		response.content().writeBytes(buffer);
		// 重置ByteBuf
		buffer.release();
		// 发送完成并主动关闭连接
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	// 重定向操作
	private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
		response.headers().set(HttpHeaderNames.LOCATION, newUri);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	// 错误信息
	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
				Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private static void setContentTypeHeader(HttpResponse response, File file) {
		// 使用mime对象获取文件对应的Content-Type
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
	}
}
