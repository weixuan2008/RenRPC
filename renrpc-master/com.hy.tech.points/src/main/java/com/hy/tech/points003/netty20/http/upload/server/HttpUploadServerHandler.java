package com.hy.tech.points003.netty20.http.upload.server;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;

public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

	private static final Logger logger = Logger.getLogger(HttpUploadServerHandler.class.getName());

	private HttpRequest request;

	private boolean readingChunks;

	private final StringBuilder responseContent = new StringBuilder();

	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // 大小超过minsize放磁盘上

	private HttpPostRequestDecoder decoder;

	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true; // 退出时是否删除临时文件
		DiskFileUpload.baseDirectory = "D:" + File.separatorChar + "aa"; // 文件存储路径

		DiskAttribute.deleteOnExitTemporaryFile = true; // 退出时是否删除临时文件
		DiskAttribute.baseDirectory = "D:" + File.separatorChar + "aa"; // 文件存储路径
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (decoder != null) {
			decoder.cleanFiles();
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpRequest) { // HttpRequest传输头
			HttpRequest request = this.request = (HttpRequest) msg;
			URI uri = new URI(request.uri());
			if (!uri.getPath().startsWith("/form")) {
				// 返回上传菜单
				writeMenu(ctx);
				return;
			}
			// 拼接反馈内容
			responseContent.setLength(0);
			responseContent.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
			responseContent.append("===================================\r\n");
			responseContent.append("VERSION: " + request.protocolVersion().text() + "\r\n");
			responseContent.append("REQUEST_URI: " + request.uri() + "\r\n\r\n");
			responseContent.append("\r\n\r\n");

			for (Entry<String, String> entry : request.headers()) {
				responseContent.append("HEADER: " + entry.getKey() + '=' + entry.getValue() + "\r\n");
			}
			responseContent.append("\r\n\r\n");

			Set<Cookie> cookies = null;
			String value = request.headers().get(HttpHeaderNames.COOKIE);
			if (value == null) {
				cookies = Collections.emptySet();
			} else {
				cookies = ServerCookieDecoder.LAX.decode(value);

			}
			for (Cookie cookie : cookies) {
				responseContent.append("COOKIE: " + cookie + "\r\n");
			}
			responseContent.append("\r\n\r\n");

			QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
			Map<String, List<String>> uriAttributes = decoderQuery.parameters();
			for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
				for (String attrVal : attr.getValue()) {
					responseContent.append("URI: " + attr.getKey() + '=' + attrVal + "\r\n");
				}
			}
			responseContent.append("\r\n\r\n");

			// GET方法, 就此return
			if (request.method().equals(HttpMethod.GET)) {
				responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");
				return;
			}

			// POST方法
			try {
				decoder = new HttpPostRequestDecoder(factory, request);
			} catch (ErrorDataDecoderException e1) {
				e1.printStackTrace();
				responseContent.append(e1.getMessage());
				writeResponse(ctx.channel());
				ctx.channel().close();
				return;
			}

			readingChunks = HttpUtil.isTransferEncodingChunked(request);
			responseContent.append("Is Chunked: " + readingChunks + "\r\n");
			responseContent.append("IsMultipart: " + decoder.isMultipart() + "\r\n");
			if (readingChunks) {
				responseContent.append("Chunks: ");
			}
		}

		if (decoder != null) {
			if (msg instanceof HttpContent) { // HttpContent具体传输的内容
				// 读取到一个chunk
				HttpContent chunk = (HttpContent) msg;
				try {
					decoder.offer(chunk);
				} catch (ErrorDataDecoderException e1) {
					e1.printStackTrace();
					responseContent.append(e1.getMessage());
					writeResponse(ctx.channel());
					ctx.channel().close();
					return;
				}
				responseContent.append('o'); // 每读一个chunk标记一个'o'
				readHttpDataChunkByChunk();
				// 最后一块chunk
				if (chunk instanceof LastHttpContent) {
					writeResponse(ctx.channel());
					readingChunks = false;
					reset();
				}
			}
		} else {
			writeResponse(ctx.channel());
		}
	}

	private void reset() {
		request = null;
		decoder.destroy(); // 释放资源
		decoder = null;
	}

	private void readHttpDataChunkByChunk() throws Exception {
		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					try {
						writeHttpData(data);
					} finally {
						data.release();
					}
				}
			}
		} catch (EndOfDataDecoderException e1) {
			responseContent.append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
		}
	}

	private void writeHttpData(InterfaceHttpData data) throws Exception {
		if (data.getHttpDataType() == HttpDataType.Attribute) {
			Attribute attribute = (Attribute) data;
			String value = null;
			try {
				value = attribute.getValue();
			} catch (IOException e1) {
				e1.printStackTrace();
				responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
						+ attribute.getName() + " Error while reading value: " + e1.getMessage() + "\r\n");
				return;
			}
			if (value.length() > 100) {
				responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
						+ attribute.getName() + " data too long\r\n");
			} else {
				responseContent.append(
						"\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": " + attribute + "\r\n");
			}
		} else {
			responseContent.append("\r\n -----------start-------------" + "\r\n");
			responseContent.append("\r\nBODY FileUpload: " + data.getHttpDataType().name() + ": " + data + "\r\n");
			responseContent.append("\r\n ------------end------------" + "\r\n");
			if (data.getHttpDataType() == HttpDataType.FileUpload) {
				FileUpload fileUpload = (FileUpload) data;
				if (fileUpload.isCompleted()) {
					System.out.println("file name : " + fileUpload.getFilename());
					System.out.println("file length: " + fileUpload.length());
					System.out.println("file maxSize : " + fileUpload.getMaxSize());
					System.out.println("file path :" + fileUpload.getFile().getPath());
					System.out.println("file absolutepath :" + fileUpload.getFile().getAbsolutePath());
					System.out.println("parent path :" + fileUpload.getFile().getParentFile());

					if (fileUpload.length() < 1024 * 1024 * 10) {
						responseContent.append("\tContent of file\r\n");
						try {
							responseContent.append(fileUpload.getString(fileUpload.getCharset()));
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						responseContent.append("\r\n");
					} else {
						responseContent.append("\tFile too long to be printed out:" + fileUpload.length() + "\r\n");
					}
					fileUpload.renameTo(new File(fileUpload.getFile().getPath())); // 核心操作, 写文件
					decoder.removeHttpDataFromClean(fileUpload);
				} else {
					responseContent.append("\tFile to be continued but should not!\r\n");
				}
			}
		}
	}

	private void writeResponse(Channel channel) {
		ByteBuf buf = Unpooled.copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
		responseContent.setLength(0);

		// 是否是短连接
		boolean close = request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE, true)
				|| request.protocolVersion().equals(HttpVersion.HTTP_1_0)
						&& !request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE, true);

		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		// 最后一次连接不需要Content-Length
		if (!close) {
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
		}

		Set<Cookie> cookies = null;
		String value = request.headers().get(HttpHeaderNames.COOKIE);
		if (value == null) {
			cookies = Collections.emptySet();
		} else {
			cookies = ServerCookieDecoder.LAX.decode(value);
		}
		if (!cookies.isEmpty()) {
			for (Cookie cookie : cookies) {
				response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookie));
			}
		}
		ChannelFuture future = channel.writeAndFlush(response);
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	// 拼接上传页html菜单
	private void writeMenu(ChannelHandlerContext ctx) {
		responseContent.setLength(0);

		// create Pseudo Menu
		responseContent.append("<html>");
		responseContent.append("<head>");
		responseContent.append("<title>Netty Test Form</title>\r\n");
		responseContent.append("</head>\r\n");
		responseContent.append("<body bgcolor=white><style>td{font-size: 12pt;}</style>");

		responseContent.append("<table border=\"0\">");
		responseContent.append("<tr>");
		responseContent.append("<td>");
		responseContent.append("<h1>Netty Test Form</h1>");
		responseContent.append("Choose one FORM");
		responseContent.append("</td>");
		responseContent.append("</tr>");
		responseContent.append("</table>\r\n");

		// GET
		responseContent.append("<CENTER>GET FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
		responseContent.append("<FORM ACTION=\"/formget\" METHOD=\"GET\">");
		responseContent.append("<input type=hidden name=getform value=\"GET\">");
		responseContent.append("<table border=\"0\">");
		responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"info\" size=10></td></tr>");
		responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"secondinfo\" size=20>");
		responseContent
				.append("<tr><td>Fill with value: <br> <textarea name=\"thirdinfo\" cols=40 rows=10></textarea>");
		responseContent.append("</td></tr>");
		responseContent.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
		responseContent.append("<td><INPUT TYPE=\"reset\" NAME=\"Clear\" VALUE=\"Clear\" ></INPUT></td></tr>");
		responseContent.append("</table></FORM>\r\n");
		responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

		// POST
		responseContent.append("<CENTER>POST FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
		responseContent.append("<FORM ACTION=\"/formpost\" METHOD=\"POST\">");
		responseContent.append("<input type=hidden name=getform value=\"POST\">");
		responseContent.append("<table border=\"0\">");
		responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"info\" size=10></td></tr>");
		responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"secondinfo\" size=20>");
		responseContent
				.append("<tr><td>Fill with value: <br> <textarea name=\"thirdinfo\" cols=40 rows=10></textarea>");
		responseContent.append(
				"<tr><td>Fill with file (only file name will be transmitted): <br> <input type=file name=\"myfile\">");
		responseContent.append("</td></tr>");
		responseContent.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
		responseContent.append("<td><INPUT TYPE=\"reset\" NAME=\"Clear\" VALUE=\"Clear\" ></INPUT></td></tr>");
		responseContent.append("</table></FORM>\r\n");
		responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

		// POST with enctype="multipart/form-data"
		responseContent.append("<CENTER>POST MULTIPART FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
		responseContent.append("<FORM ACTION=\"/formpostmultipart\" ENCTYPE=\"multipart/form-data\" METHOD=\"POST\">");
		responseContent.append("<input type=hidden name=getform value=\"POST\">");
		responseContent.append("<table border=\"0\">");
		responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"info\" size=10></td></tr>");
		responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"secondinfo\" size=20>");
		responseContent
				.append("<tr><td>Fill with value: <br> <textarea name=\"thirdinfo\" cols=40 rows=10></textarea>");
		responseContent.append("<tr><td>Fill with file: <br> <input type=file name=\"myfile\">");
		responseContent.append("</td></tr>");
		responseContent.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
		responseContent.append("<td><INPUT TYPE=\"reset\" NAME=\"Clear\" VALUE=\"Clear\" ></INPUT></td></tr>");
		responseContent.append("</table></FORM>\r\n");
		responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

		responseContent.append("</body>");
		responseContent.append("</html>");

		ByteBuf buf = Unpooled.copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
		response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

		ctx.channel().writeAndFlush(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.log(Level.WARNING, responseContent.toString(), cause);
		ctx.channel().close();
	}
}