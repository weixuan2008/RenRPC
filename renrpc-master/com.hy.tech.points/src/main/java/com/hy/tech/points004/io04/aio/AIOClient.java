package com.hy.tech.points004.io04.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

public class AIOClient {
	private final AsynchronousSocketChannel client;
	private int port = 9000;

	public AIOClient() throws IOException {
		client = AsynchronousSocketChannel.open();
	}

	public void start() throws IOException {
		client.connect(new InetSocketAddress("localhost", port), null, new CompletionHandler<Void, Void>() {

			@Override
			public void completed(Void result, Void attachment) {
				try {
					client.write(ByteBuffer.wrap("I am client".getBytes())).get();
					System.out.println("Sent data to server");
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				exc.printStackTrace();
			}

		});

		final ByteBuffer buffer = ByteBuffer.allocate(1024);
		client.read(buffer, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
				System.out.println(result);
				System.out.println("Got response from server: " + new String(buffer.array()));
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				exc.printStackTrace();
			}

		});
	}

	public static void main(String[] args) throws IOException {
		new AIOClient().start();

		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
