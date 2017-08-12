package com.hy.tech.points001.proxy00.reflect;

public class Person {
	private int i = 1;
	private static int b = 2;

	public Person() {
		System.out.println("无参构造");
	}

	private Person(String s) {
		System.out.println("有参构造" + s);
	}

	public void say() {
		System.out.println("say");
	}
}
