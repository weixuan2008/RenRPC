package com.hy.rng.service.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RNGService {
	private static final Logger logger = LoggerFactory.getLogger(RNGService.class);
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		logger.info("Starting Rng service...");
		new ClassPathXmlApplicationContext("spring-rng-service.xml");
	}

}
