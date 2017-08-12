package com.hy.tech.points004.io02.pseudo.aio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TimeServerHandler implements Runnable {
	private Socket socket;

	public TimeServerHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;

		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			out = new PrintWriter(this.socket.getOutputStream(), true);

			String currentTime = null;
			String body = null;

			while (true) {
				body = in.readLine(); // 从客户端读
				if (body == null)
					break;
				System.out.println("This time server received message:" + body);

				currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
						? new java.util.Date(System.currentTimeMillis()).toString()
						: "BAD ORDER";
				out.println(currentTime);
			}

		} catch (Exception ex) {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			if (out != null) {
				out.close();
				out = null;
			}

			if (this.socket != null) {
				try {
					this.socket.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				this.socket = null;
			}
		}

	}

}
