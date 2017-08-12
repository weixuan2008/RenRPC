package com.hy.ren.rpc.cluster.loadbalance;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;

import com.hy.ren.rpc.cluster.common.LoadBlance;
import com.hy.ren.rpc.common.ZKData;

public class LDWithLeastConnection implements LoadBlance {
	private ZkClient zkClient;
	private String serviceName;
	
	public LDWithLeastConnection(ZkClient zkClient, String serviceName) {
		this.zkClient = zkClient;
		this.serviceName = serviceName;
	}
	
	@Override
	public String select() {
		List<String> serverList = zkClient.getChildren(ZKData.ZK_REGISTRY_PATH + "/" + serviceName);

		String tempServer = null;
		int tempConn = -1;
		for (int i = 0; i < serverList.size(); i++) {
			String server = serverList.get(i);
			if (zkClient.readData(ZKData.ZK_REGISTRY_PATH + "/" + server) != null) {
				int connNum = zkClient.readData(ZKData.ZK_REGISTRY_PATH + "/" + server);
				if (tempConn == -1) {
					tempServer = server;
					tempConn = connNum;
				}
				if (connNum < tempConn) {
					tempServer = server;
					tempConn = connNum;
				}
			} else {
				zkClient.close();
				return server;
			}
		}
		
		zkClient.close();
		if(tempServer != null && !tempServer.equals("")) {
			return tempServer;
		}
		
		return null;

	}

}
