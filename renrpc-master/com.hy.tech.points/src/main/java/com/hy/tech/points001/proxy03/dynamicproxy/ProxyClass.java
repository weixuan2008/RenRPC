package com.hy.tech.points001.proxy03.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

// Dynamic proxy class that implements InvocationHandler interface.
public class ProxyClass implements InvocationHandler {
	AbstractClass ac;
	
	public ProxyClass(AbstractClass ac) {
		this.ac = ac;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		// before invoking, you can do something
		method.invoke(ac, args);
		// after invoking, you can do something also.
		
		return null;
	}

}
