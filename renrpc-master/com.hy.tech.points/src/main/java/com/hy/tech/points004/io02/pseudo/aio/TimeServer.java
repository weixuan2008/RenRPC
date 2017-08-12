package com.hy.tech.points004.io02.pseudo.aio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeServer {
	public static void main(String[] args) throws IOException {
		int port = 9000;

		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("The time server started on port: " + port);

			Socket socket = null;
			
			// 创建IO任务线程池
			TimeServerHandlerExecutorPool singleExecutor = new TimeServerHandlerExecutorPool(50, 10000);
			while (true) {
				socket = server.accept();
				singleExecutor.execute(new TimeServerHandler(socket));
			}
		} finally {
			if (server != null) {
				System.out.println("The time server closed.");
				server.close();
				server = null;
			}
		}
	}
}
