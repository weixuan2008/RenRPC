package com.hy.tech.points004.io02.pseudo.aio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TimeClient {
	public static void main(String[] args) {
		int port = 9000;

		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;

		try {
			socket = new Socket("192.168.56.1", port); // 创建socket
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("QUERY TIME ORDER"); // 向服务端写出QUERY TIME ORDER

			System.out.println("Send order to server succeed.");

			String resp = in.readLine(); // 从服务端读入
			System.out.print("Get response from server, now time is:" + resp);
		} catch (Exception ex) {
			System.out.print(ex.getMessage());
		} finally {
			if (out != null) {
				out.close();
				out = null;
			}

			if (in != null) {
				try {
					in.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				in = null;
			}

			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e3) {
					e3.printStackTrace();
				}
				socket = null;
			}
		}

	}
}