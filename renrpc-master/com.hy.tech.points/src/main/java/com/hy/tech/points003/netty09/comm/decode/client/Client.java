package com.hy.tech.points003.netty09.comm.decode.client;

import java.io.File;
import java.io.FileInputStream;

import com.hy.tech.points003.netty07.comm.decode.common.GzipUtils;
import com.hy.tech.points003.netty07.comm.decode.common.MarshallingCodeCFactory;
import com.hy.tech.points003.netty07.comm.decode.common.Request;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {
	 public static void main(String[] args) throws Exception{
	        
	        EventLoopGroup group = new NioEventLoopGroup();
	        Bootstrap b = new Bootstrap();
	        b.group(group)
	         .channel(NioSocketChannel.class)
	         .handler(new ChannelInitializer<SocketChannel>() {
	            @Override
	            protected void initChannel(SocketChannel sc) throws Exception {
	                sc.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
	                sc.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
	                sc.pipeline().addLast(new ClientHandler());
	            }
	        });
	        
	        ChannelFuture cf = b.connect("127.0.0.1", 8765).sync();
	        
	        for(int i = 0; i < 5; i++){
	            Request req = new Request();
	            req.setId("" + i);
	            req.setName("req" + i);
	            req.setRequestMessage("数据信息" + i);    
	            String path = System.getProperty("user.dir") + File.separatorChar + "sources" +  File.separatorChar + "001.jpg";
	            File file = new File(path);
	            FileInputStream in = new FileInputStream(file);  
	            byte[] data = new byte[in.available()];  
	            in.read(data);  
	            in.close(); 
	            req.setAttachment(GzipUtils.gzip(data)); //压缩
	            cf.channel().writeAndFlush(req);
	        }

	        cf.channel().closeFuture().sync();
	        group.shutdownGracefully();
	    }
}
