package com.hy.ren.rpc.cluster.hash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class ConsistentHash {
	private String client;
	private List<String> servers;

	public ConsistentHash(String client, List<String> servers) {
		this.client = client;
		this.servers = servers;
	}

	public String select() {
		String key = client;
		byte[] digest = md5(key);
		String server = servers.get((int) hash(digest, 0));

		return server;
	}

	private long hash(byte[] digest, int number) {
		return (((long) (digest[3 + number * 4] & 0xFF) << 24) | ((long) (digest[2 + number * 4] & 0xFF) << 16)
				| ((long) (digest[1 + number * 4] & 0xFF) << 8) | (digest[0 + number * 4] & 0xFF)) & 0xFFFFFFFFL;
	}

	private byte[] md5(String value) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		md5.reset();
		byte[] bytes = null;
		try {
			bytes = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		md5.update(bytes);

		return md5.digest();
	}
}
