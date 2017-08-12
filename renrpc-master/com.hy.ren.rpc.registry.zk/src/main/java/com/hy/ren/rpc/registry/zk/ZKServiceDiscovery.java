package com.hy.ren.rpc.registry.zk;

import java.util.List;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hy.ren.rpc.cluster.loadbalance.LDWithRoundRobin;
import com.hy.ren.rpc.common.ZKData;
import com.hy.ren.rpc.registry.ServiceDiscovery;
import com.hy.ren.rpc.utils.CollectionUtil;

/**
 * 
 * Description: The implementation for discover service that based on zookeeper.
 *
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */

public class ZKServiceDiscovery implements ServiceDiscovery {
	private static final Logger logger = LoggerFactory.getLogger(ZKServiceDiscovery.class);
	private String zkAddress;
	private ZkClient zkClient;

	public ZKServiceDiscovery(String zkAddress) {
		this.zkAddress = zkAddress;
		connect();
	}

	private void connect() {
		// Connect to zookeeper
		zkClient = new ZkClient(zkAddress, ZKData.ZK_SESSION_TIMEOUT, ZKData.ZK_CONNECTION_TIMEOUT);
		logger.info("Connected zookeeper");
	}

	@Override
	public String discover(String serviceName) {
		try {
			// Try to retrieve service node
			String servicePath = ZKData.ZK_REGISTRY_PATH + "/" + serviceName;
			if (!zkClient.exists(servicePath)) {
				//throw new RuntimeException(String.format("Cannot find any service node on path: %s", servicePath));
				logger.error(String.format("Cannot find any service node on path: %s", servicePath));
				return null;
			}

			List<String> addressList = zkClient.getChildren(servicePath);
			if (CollectionUtil.isEmpty(addressList)) {
				//throw new RuntimeException(String.format("Cannot find any address node on path: %s", servicePath));
				logger.error(String.format("Cannot find any address node on path: %s", servicePath));
				return null;
			}
			
			// Here, using round robin algorithm to do load balance.
			LDWithRoundRobin balancer = new LDWithRoundRobin(zkClient, serviceName);
			String address = balancer.select();
			
			// Get the value of address
			String addressPath = servicePath + "/" + address;
			return zkClient.readData(addressPath);
			
		} finally {
			//zkClient.close();
		}

	}

	@Override
	public void failOver(String serviceName) {
		zkClient.subscribeChildChanges(ZKData.ZK_REGISTRY_CLIENT, new IZkChildListener() {
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				/*boolean has = false;
				for (int i = 0; i < currentChilds.size(); i++) {
					if (getServer().equals(currentChilds.get(i))) {
						has = true;
						break;
					}
				}
				if (!has) {
					discover(zkClient);
				}*/
			}
		});

	}

	@Override
	public String getZkAddress() {
		return zkAddress;
	}
}