package com.hy.rng.api;

/**
 * 
 * Description: The interface for RNG service.
 *
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 25, 2017
 */
public interface Rng {
	/**
	 * Description: Return "true" or "false".
	 * 
	 * @return boolean
	 */
	boolean nextBoolean();

	/**
	 * Description: Return a int value that Integer.MIN_VALUE <= n <=
	 * Integer.MAX_VALUE.
	 * 
	 * @return int
	 */
	int nextInt();

	/**
	 * Description: return a int value that 0 <= m < max.
	 * 
	 * @param int
	 * @return int
	 */
	int nextInt(int max);

	/**
	 * Description: Return a long value that Long.MIN_VALUE <= n <= Long.MAX_VALUE.
	 * 
	 * @return long
	 */
	long nextLong();

	/**
	 * Description: Return a long value that 0 <= m < max.
	 * 
	 * @param long
	 * @return long
	 */
	long nextLong(long max);

	/**
	 * Description: Return a float value that 0 <= x < 1.
	 * 
	 * @return float
	 */
	float nextFloat();

	/**
	 * Description: Returna a double that 0 <= x < 1.
	 * 
	 * @param
	 * @return double
	 */
	double nextDouble();

}
