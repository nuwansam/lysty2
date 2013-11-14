package org.lysty.util;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;

public class Utils {

	public static final int EDIT_DISTANCE_THRESHOLD = 5;
	public static Logger logger = Logger.getLogger(Utils.class);

	public static int editDistance(String s, String t) {
		int m = s.length();
		int n = t.length();
		int[][] d = new int[m + 1][n + 1];
		for (int i = 0; i <= m; i++) {
			d[i][0] = i;
		}
		for (int j = 0; j <= n; j++) {
			d[0][j] = j;
		}
		for (int j = 1; j <= n; j++) {
			for (int i = 1; i <= m; i++) {
				if (s.charAt(i - 1) == t.charAt(j - 1)) {
					d[i][j] = d[i - 1][j - 1];
				} else {
					d[i][j] = min((d[i - 1][j] + 1), (d[i][j - 1] + 1),
							(d[i - 1][j - 1] + 1));
				}
			}
		}
		return (d[m][n]);
	}

	public static int min(int a, int b, int c) {
		return (Math.min(Math.min(a, b), c));
	}

	public static boolean stringNotNullOrEmpty(String artist1) {
		// TODO Auto-generated method stub
		return !(artist1 == null || artist1.trim().isEmpty());
	}

	public static String getArgsLogString(String[] args) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			builder.append("arg ").append(i).append(" ").append(args[i])
					.append(" ");
		}
		return builder.toString();
	}

	public static Icon getIcon(String iconName) {
		try {
			ImageIcon icon = new ImageIcon(System.getProperty("user.dir")
					+ "/resources/icons/" + iconName);
			return icon;
		} catch (Exception e) {
			logger.error("Error loading icon for: " + iconName, e);
		}
		return null;
	}

	public static boolean isNumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean shutdown(int time) throws IOException {
		String shutdownCommand = null, t = time == 0 ? "now" : String
				.valueOf(time);

		if (SystemUtils.IS_OS_AIX)
			shutdownCommand = "shutdown -Fh " + t;
		else if (SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_LINUX
				|| SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX
				|| SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD
				|| SystemUtils.IS_OS_UNIX)
			shutdownCommand = "shutdown -h " + t;
		else if (SystemUtils.IS_OS_HP_UX)
			shutdownCommand = "shutdown -hy " + t;
		else if (SystemUtils.IS_OS_IRIX)
			shutdownCommand = "shutdown -y -g " + t;
		else if (SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_SUN_OS)
			shutdownCommand = "shutdown -y -i5 -g" + t;
		else if (SystemUtils.IS_OS_WINDOWS_XP
				|| SystemUtils.IS_OS_WINDOWS_VISTA
				|| SystemUtils.IS_OS_WINDOWS_7)
			shutdownCommand = "shutdown.exe -s -t " + t;
		else
			return false;

		logger.info("Attempting to shutdown");
		Runtime.getRuntime().exec(shutdownCommand);
		return true;
	}
}
