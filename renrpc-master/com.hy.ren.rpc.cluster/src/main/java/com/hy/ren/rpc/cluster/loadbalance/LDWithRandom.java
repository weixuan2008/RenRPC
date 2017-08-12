package com.hy.ren.rpc.cluster.loadbalance;

import java.util.List;
import java.util.Random;

import org.I0Itec.zkclient.ZkClient;

import com.hy.ren.rpc.cluster.common.LoadBlance;
import com.hy.ren.rpc.common.ZKData;

public class LDWithRandom implements LoadBlance {
	private ZkClient zkClient;
	private String serviceName;
	
	public LDWithRandom(ZkClient zkClient, String serviceName) {
		this.zkClient = zkClient;
		this.serviceName = serviceName;
	}
	
	@Override
	public String select() {
		List<String> serverList = zkClient.getChildren(ZKData.ZK_REGISTRY_PATH + "/" + serviceName);

		Random random = new Random();
		if (serverList.size() >= 1) {
			String server = serverList.get(random.nextInt(serverList.size()));
			return server;
		} else {
			return null;
		}

	}
}
