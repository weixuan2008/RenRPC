package com.hy.ren.rpc.registry;

/**
 * 
 * Description: Interface for registry service.
 *
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public interface ServiceRegistry {

	public void register(String serviceName, String serviceAddress);

}
