package com.hy.rng.service.impl;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

import com.hy.ren.rpc.common.RpcService;
import com.hy.rng.api.Rng;


@RpcService(Rng.class)// Remote interface
public class RngImpl implements Rng {
	private UniformRandomProvider rng ;
	private static RngImpl INSTANCE = new RngImpl();
	
	private RngImpl() {
		// Instantiate a "Mersenne-Twister" generator with a factory method.
		rng = RandomSource.create(RandomSource.MT);
	}
	public static RngImpl getInstance() {
		return INSTANCE;
	}
	
	@Override
	public boolean nextBoolean() {
		return rng.nextBoolean();
	}

	@Override
	public int nextInt() {
		return rng.nextInt();
	}

	@Override
	public int nextInt(int max) {
		return rng.nextInt(max);
	}

	@Override
	public long nextLong() {
		return rng.nextLong();
	}

	@Override
	public long nextLong(long max) {
		return rng.nextLong(max);
	}

	@Override
	public float nextFloat() {
		return rng.nextFloat();
	}

	@Override
	public double nextDouble() {
		return rng.nextDouble();
	}
}
