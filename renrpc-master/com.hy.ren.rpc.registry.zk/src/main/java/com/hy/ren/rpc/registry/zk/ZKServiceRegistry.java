package com.hy.ren.rpc.registry.zk;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hy.ren.rpc.common.ZKData;
import com.hy.ren.rpc.registry.ServiceRegistry;

/**
 * 
 * Description: The implementation for registry service that based on zookeeper.
 *
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public class ZKServiceRegistry implements ServiceRegistry {
	private static final Logger logger = LoggerFactory.getLogger(ZKServiceRegistry.class);
	private final ZkClient zkClient;

	public ZKServiceRegistry(String zkAddress) {
		// Create zookeeper client
		zkClient = new ZkClient(zkAddress, ZKData.ZK_SESSION_TIMEOUT, ZKData.ZK_CONNECTION_TIMEOUT);
		logger.info("Connected to zookeeper server.");
	}

	@Override
	public void register(String serviceName, String serviceAddress) {
		// Create registry node(persistent).
		String registryPath = ZKData.ZK_REGISTRY_PATH;
		if (!zkClient.exists(registryPath)) {
			zkClient.createPersistent(registryPath);
			logger.info("Created registry node: {}", registryPath);
		}

		// Create service node(persistent).
		String servicePath = registryPath + "/" + serviceName;
		if (!zkClient.exists(servicePath)) {
			zkClient.createPersistent(servicePath);
			logger.info("Create service node: {}", servicePath);
		}

		// Create address node(Ephemeral)
		String addressPath = servicePath + "/address-";
		String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
		logger.info("Create address node: {}", addressNode);
	}

}