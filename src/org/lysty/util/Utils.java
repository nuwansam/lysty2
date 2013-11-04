package org.lysty.util;

public class Utils {

	public static final int EDIT_DISTANCE_THRESHOLD = 5;

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
}
