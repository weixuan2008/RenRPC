package com.hy.ren.rpc.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hy.ren.rpc.common.PacketType;
import com.hy.ren.rpc.model.Request;
import com.hy.ren.rpc.model.Response;
import com.hy.ren.rpc.registry.ServiceDiscovery;
import com.hy.ren.rpc.utils.HashUtil;
import com.hy.ren.rpc.utils.StringUtil;

public class RpcProxy {

	private static final Logger logger = LoggerFactory.getLogger(RpcProxy.class);

	// private String serviceAddress;

	private ServiceDiscovery serviceDiscovery;
	private RpcConsumer client;

	/*
	 * public RpcProxy(String serviceAddress) { this.serviceAddress =
	 * serviceAddress; }
	 */

	public RpcProxy(ServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	private void initRpcProxy(final Class<?> interfaceClass, final String serviceVersion) throws InterruptedException {
		String serviceAddress = null;
		
		// Get RPC service address
		if (serviceDiscovery != null) {
			String serviceName = interfaceClass.getName();
			
			if (StringUtil.isNotEmpty(serviceVersion)) {
				serviceName += "-" + serviceVersion;
			}

			// Get address for specified service.
			while(serviceAddress == null) {
				serviceAddress = serviceDiscovery.discover(serviceName);
				if(serviceAddress == null) {
					logger.warn(String.format("Waiting for service: %s on RD service: %s", serviceName, serviceDiscovery.getZkAddress()));
					Thread.sleep(10000);
				}
			}

			// Begin to watch this node for failOver.
			serviceDiscovery.failOver(serviceName);

			logger.info("discover service: {} => {}", serviceName, serviceAddress);
		}

		if (StringUtil.isEmpty(serviceAddress)) {
			throw new RuntimeException("server address is empty");
		}

		// Parse host name and port from the address of RPC service
		String[] array = StringUtil.split(serviceAddress, ":");
		String host = array[0];
		int port = Integer.parseInt(array[1]);

		// Create RPC client-side object
		client = new RpcConsumer(host, port);
		client.init();
	}

	@SuppressWarnings({ "unchecked", "unused" })
	public <T> T create(final Class<?> interfaceClass, final String serviceVersion) throws InterruptedException {
		initRpcProxy(interfaceClass, serviceVersion);

		// Create dynamic proxy object
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
				new InvocationHandler() {

					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// Create RPC request object and set parameters.
						Request request = new Request();
						request.setId(HashUtil.bytes2Hex(HashUtil.md5(UUID.randomUUID().toString())));
						request.setPacketType(PacketType.PT_REQUEST);
						request.setInterfaceName(method.getDeclaringClass().getName());
						request.setServiceVersion(serviceVersion);
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);

						Response response = client.send(request);

						if (response == null) {
							throw new RuntimeException("response is null");
						}
						// Return RPC response result
						if (response.getException() != null) {
							throw response.getException();
						} else {
							return response.getResult();
						}
					}

				});
	}

}
