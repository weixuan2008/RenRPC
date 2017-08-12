package com.hy.ren.rpc.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Description:
 * 
 * @author Eddie.Wei
 * @version 1.0
 * @since Aug 10, 2017
 */
public class HashUtil {
	public static byte[] md5(String message) {
		try {
			// 1 创建一个提供信息摘要算法的对象，初始化为md5算法对象
			MessageDigest md = MessageDigest.getInstance("MD5");

			// 2 将消息变成byte数组
			byte[] input = message.getBytes();

			// 3 计算后获得字节数组,这就是那128位了
			byte[] buff = md.digest(input);

			return buff;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Description: TODO
	 * 
	 * @param
	 * @return String
	 */
	public static String base64EncodeStr(String plainText) {
		byte[] b = plainText.getBytes();
		Base64 base64 = new Base64();
		b = base64.encode(b);
		String s = new String(b);
		return s;
	}

	/**
	 * Description: TODO
	 * 
	 * @param
	 * @return String
	 */
	public static String base64DecodeStr(String encodeStr) {
		byte[] b = encodeStr.getBytes();
		Base64 base64 = new Base64();
		b = base64.decode(b);
		String s = new String(b);
		return s;
	}

	/**
	 * Description: TODO
	 * 
	 * @param
	 * @return String
	 */
	public static String base64EncodeBytes(byte[] b) {
		Base64 base64 = new Base64();
		b = base64.encode(b);
		String s = new String(b);
		return s;
	}

	/**
	 * Description: TODO
	 * 
	 * @param
	 * @return String
	 */
	public static String base64DecodeBytes(byte[] b) {
		Base64 base64 = new Base64();
		b = base64.decode(b);
		String s = new String(b);
		return s;
	}

	/**
	 * 
	 * Description: TODO
	 * 
	 * @param
	 * @return String
	 */
	public static String bytes2Hex(byte[] b) {
		return Hex.encodeHexString(b);
	}

}
