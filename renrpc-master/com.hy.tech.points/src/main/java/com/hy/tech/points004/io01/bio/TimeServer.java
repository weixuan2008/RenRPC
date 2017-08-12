package com.hy.tech.points004.io01.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TimeServer {
	private static List<Socket> sockets = new ArrayList<Socket>();
	
	public static void main(String[] args) throws IOException {
		int port = 9000;

		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("The time server started on port: " + port);

			Socket socket = null;
			while (true) {
				socket = server.accept(); // 接收客户端连接请求，没有的时候就阻塞
				
				{
					sockets.add(socket);
					System.out.println("Get connection from client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() );
				}
				
				new Thread(new TimeServerHandler(socket)).start();
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
