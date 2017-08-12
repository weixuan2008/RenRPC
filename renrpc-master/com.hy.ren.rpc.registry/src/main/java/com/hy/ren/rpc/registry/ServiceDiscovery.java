package com.hy.ren.rpc.registry;

/**
 * 
 * Description: Interface for discovery service.
 *
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public interface ServiceDiscovery {
	String getZkAddress();
	String discover(String serviceName);
	void failOver(String serviceName);
}