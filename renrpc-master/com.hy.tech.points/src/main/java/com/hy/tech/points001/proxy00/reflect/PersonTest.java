package com.hy.tech.points001.proxy00.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PersonTest {
	public static void main(String[] args) throws Exception {
		// 返回A的构造方法
		Constructor c = Person.class.getConstructor();
		
		// 返回A类的所有为public 声明的构造方法
		Constructor[] cons = Person.class.getConstructors();
		
		// 返回A类所有的构造方法，包括private
		Constructor[] cons2 = Person.class.getDeclaredConstructors();
		
		// 返回A类的第一个public 方法
		Method m = Person.class.getMethod("say");
		
		// 执行
		m.invoke(Person.class.newInstance());
		
		// 返回A类所有的public 方法
		Method[] ms = Person.class.getMethods();
		
		// 返回A类所有的方法，包括private
		Method[] allMs = Person.class.getDeclaredMethods();
		
		// 返回A类的public字段
		Field field2 = Person.class.getDeclaredField("i");
		System.out.println(field2.get(Person.class.newInstance()));
		
		Field field = Person.class.getField("i");
		System.out.println(field.get(Person.class.newInstance()));
		
		// 返回A类的static 字段
		System.out.println(field.get(null));
	}
}
