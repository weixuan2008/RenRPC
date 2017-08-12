package com.hy.ren.rpc.common;

public interface ZKData {
	/**
	 * The general configuration for zookeeper
	 */
	int ZK_SESSION_TIMEOUT = 5000;
	int ZK_CONNECTION_TIMEOUT = 1000;

	/**
	 * The root node for all services using renrpc framework
	 */
	String ZK_REGISTRY_PATH = "/renrpc_registry";
	
	/**
	 * The index for load balance using round robin algorithm.
	 */
	String ZK_REGISTRY_ROUND = "/renrpc_round";
	
	/**
	 * The index for load balance using round robin algorithm.
	 */
	String ZK_REGISTRY_CLIENT = "/renrpc_client";
	
	/**
	 * The index for load balance using round robin algorithm.
	 */
	String ZK_REGISTRY_ROUTE = "/renrpc_route";
}
