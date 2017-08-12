package com.hy.ren.rpc.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description: RPC service annotation, should mark it on implementation class.
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 25, 2017
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component  // Indicated it can be scanned by spring
public @interface RpcService {
	/**
	 * Description: Interface class for service.
	 * @param 
	 * @return Class<?>
	 */
	Class<?> value();
	
	/**
	 * Description: version for service.
	 * @param 
	 * @return String
	 */
	String version() default "";
}
