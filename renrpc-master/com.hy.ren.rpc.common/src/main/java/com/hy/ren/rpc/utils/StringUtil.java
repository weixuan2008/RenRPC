package com.hy.ren.rpc.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * Description: The utility for String.
 *
 * @author Eddie.Wei
 * @version 1.0
 * @since Jul 26, 2017
 */
public class StringUtil {
	public static boolean isEmpty(String str) {
		if (str != null)
			str = str.trim();

		return StringUtils.isEmpty(str);
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static String[] split(String str, String separator) {
		return StringUtils.splitByWholeSeparator(str, separator);
	}
}
