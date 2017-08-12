package com.hy.tech.points002.protobuf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LoginUserTest {

	public static void main(String[] args) throws Exception {
		LoginUserTest test = new LoginUserTest();
		//test.write();
		
		test.read();
	}
	
	public void write() throws Exception {
		// 1. build login message object
		LoginUser.Login.Builder builder = LoginUser.Login.newBuilder();
		builder.setUsername("Eddie");
		builder.setPwd(123);
		
		// 2. serialize this object to disk
		FileOutputStream output = new FileOutputStream("./loginUser.data");
		builder.build().writeTo(output);
		output.close();
	}
	
	public void read() throws Exception{
		FileInputStream input = new FileInputStream("./loginUser.data");
		LoginUser.Login login = LoginUser.Login.parseFrom(input);
		System.out.println("login.username: " + login.getUsername());
		System.out.println("login.pwd: " + login.getPwd());
	}

}
