package com.hy.ren.rpc.cluster.loadbalance;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;

import com.hy.ren.rpc.cluster.common.LoadBlance;
import com.hy.ren.rpc.cluster.hash.ConsistentHash;
import com.hy.ren.rpc.common.ZKData;

/**
 * Description: ConsistentHash LoadBalance
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 30, 2017
 */
public class LDWithConsistentHash implements LoadBlance {
	private ZkClient zkClient;
	private String client;
	private String serviceName;
	
	public LDWithConsistentHash(ZkClient zkClient, String client, String serviceName) {
		this.zkClient = zkClient;
		this.client = client;
		this.serviceName = serviceName;
	}
	
	
	@Override
	public String select() {
		List<String> serverList = zkClient.getChildren(ZKData.ZK_REGISTRY_PATH + "/" + serviceName);
		ConsistentHash selector = new ConsistentHash(client, serverList);
		
		return selector.select();
	}

}
