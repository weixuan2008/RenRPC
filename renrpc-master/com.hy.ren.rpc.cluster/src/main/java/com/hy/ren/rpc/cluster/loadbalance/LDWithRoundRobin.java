package com.hy.ren.rpc.cluster.loadbalance;

import java.util.List;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hy.ren.rpc.cluster.common.LoadBlance;
import com.hy.ren.rpc.common.ZKData;

public class LDWithRoundRobin implements LoadBlance {
	private static final Logger logger = LoggerFactory.getLogger(LDWithRoundRobin.class);

	private ZkClient zkClient;
	private String serviceName;

	public LDWithRoundRobin(ZkClient zkClient, String serviceName) {
		this.zkClient = zkClient;
		this.serviceName = serviceName;
	}

	@Override
	public String select() {
		List<String> serverList = zkClient.getChildren(ZKData.ZK_REGISTRY_PATH + "/" + serviceName);
		int round = 0;

		// Create root node for round
		if (!zkClient.exists(ZKData.ZK_REGISTRY_ROUND)) {
			zkClient.createPersistent(ZKData.ZK_REGISTRY_ROUND);
			logger.info("Created root node: {} for round.", ZKData.ZK_REGISTRY_ROUND);
		}

		// Create service node for round.
		String servicePath = ZKData.ZK_REGISTRY_ROUND + "/" + serviceName;
		if (!zkClient.exists(servicePath)) {
			zkClient.createPersistent(servicePath);
			zkClient.writeData(servicePath, 0);
		} else {
			round = (Integer) zkClient.readData(servicePath);
			zkClient.writeData(servicePath, ++round);
		}


		if (serverList != null && serverList.size() > 0) {
			return serverList.get(round % serverList.size());
		} else {
			return null;
		}
	}

}
