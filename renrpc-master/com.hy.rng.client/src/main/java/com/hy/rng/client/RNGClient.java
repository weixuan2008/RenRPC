package com.hy.rng.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hy.ren.rpc.consumer.RpcProxy;
import com.hy.rng.api.Rng;

public class RNGClient {
	private static final Logger logger = LoggerFactory.getLogger(RNGClient.class);

	public static void main(String[] args) {
		logger.info("The Rng client is running...");

		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-rng-client.xml");
		RpcProxy rpcProxy = context.getBean(RpcProxy.class);

		Rng rng = null;
		try {
			rng = rpcProxy.create(Rng.class, "");
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}

		while (true) {
			try {
				//logger.info("Get rng double value(" + rng.nextDouble() + ") from Rng service.");
				//logger.info("Get rng long value(" + rng.nextLong() + ") from Rng service.");
				Thread.sleep(5 * 1000);
			} catch (Exception ex) {
				logger.error("RNGClient,{}", ex.getMessage());
			}
		}
	}
}
