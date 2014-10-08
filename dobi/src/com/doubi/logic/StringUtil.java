package com.doubi.logic;

import java.io.IOException;
import java.io.InputStream;

/**
 * 字符型处理类
 * 
 * @author Administrator
 *
 */
public class StringUtil {

	/**
	 * 将InputStream转换为字符串
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String InputStreamToString(InputStream in) throws IOException {

		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		int n;
		while ((n = in.read(b)) != -1) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}
}
