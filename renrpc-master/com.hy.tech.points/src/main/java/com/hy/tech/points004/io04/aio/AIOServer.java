package com.hy.tech.points004.io04.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AIOServer {
	private static int port = 9000;

	private void listen() throws IOException {
		ExecutorService executorService = Executors.newCachedThreadPool();
		AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);

		AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(threadGroup);
		server.bind(new InetSocketAddress(port));

		System.out.println("AIOServer started on port: " + port);

		server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
			final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

			@Override
			public void completed(AsynchronousSocketChannel result, Object attachment) {
				System.out.println("waiting for connection from client....");

				buffer.clear();
				try {
					result.read(buffer).get();
					//System.out.println("Got message from client: " + new String(buffer.array()));
					
					buffer.flip();

					result.write(ByteBuffer.wrap("hello guy, welcome".getBytes())).get();
					buffer.flip();
					
				} catch (InterruptedException | ExecutionException e) {
					System.out.println(e.toString());
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				System.out.println("AIOServer failed: " + exc);
			}

		});
	}

	public static void main(String[] args) throws IOException {
		new AIOServer().listen();

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
