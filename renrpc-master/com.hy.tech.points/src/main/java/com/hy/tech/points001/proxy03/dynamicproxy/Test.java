package com.hy.tech.points001.proxy03.dynamicproxy;

import java.lang.reflect.Proxy;

public class Test {

	public static void main(String[] args) {
		// create handle object for concrete class A
		ProxyClass proxyClass = new ProxyClass(new ClassA());

		// get proxy for concrete class ClassA
		AbstractClass ac1 = (AbstractClass) Proxy.newProxyInstance(AbstractClass.class.getClassLoader(),
				new Class[] { AbstractClass.class }, proxyClass);
		
		// invoke show method of ClassB
		ac1.show();

		// create handle object for concrete class B
		ProxyClass invoker2 = new ProxyClass(new ClassB());
		
		// get proxy for concrete class ClassB
		AbstractClass ac2 = (AbstractClass) Proxy.newProxyInstance(AbstractClass.class.getClassLoader(),
				new Class[] { AbstractClass.class }, invoker2);
		
		// invoke show method of ClassB
		ac2.show();
	}

}
