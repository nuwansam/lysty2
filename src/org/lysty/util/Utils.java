package org.lysty.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.lysty.dao.Song;

public class Utils {

	public static final int EDIT_DISTANCE_THRESHOLD = 5;
	private static final String LYSTY_FOLDER = "lysty";
	public static final String PLUGINS_FOLDER = "plugins";
	public static final String EXTRACTORS_FOLDER = "extractors";
	public static final String STRATEGIES_FOLDER = "strategies";
	public static final String LOGS_FOLDER = "logs";
	public static final String SETTINGSFILE = "settings.properties";
	public static final String DB_FOLDER = "db";
	private static final String EXTRACTOR_JAR = "extractor.jar";
	private static final String STRATEGY_JAR = "strategy.jar";
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

	public static File getAppDirectoryFolder(String folderName) {
		String workingDirectory;
		String lystyFolder = null;
		// here, we assign the name of the OS, according to Java, to a
		// variable...
		String OS = (System.getProperty("os.name")).toUpperCase();
		// to determine what the workingDirectory is.
		// if it is some version of Windows
		if (OS.contains("WIN")) {
			lystyFolder = LYSTY_FOLDER;
			// it is simply the location of the "AppData" folder
			workingDirectory = System.getenv("AppData");
		}
		// Otherwise, we assume Linux or Mac
		else {
			// in either case, we would start in the user's home directory
			workingDirectory = System.getProperty("user.home");
			lystyFolder = "." + LYSTY_FOLDER;
			// if we are on a Mac, we are not done, we look for
			// "Application Support"
			if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
				workingDirectory += "/Library/Application Support";
			}
		}
		workingDirectory = workingDirectory + File.separator + lystyFolder;
		File file = new File(workingDirectory + File.separator + folderName);
		if (!file.exists()) {
			boolean b = createAppdataFolder(workingDirectory);
		}
		file = new File(workingDirectory + File.separator + folderName);
		return file;
	}

	private static boolean createAppdataFolder(String workingDir) {
		File file = new File(workingDir);
		boolean b = file.mkdir();
		if (!b)
			return false;
		file = new File(workingDir + File.separator + DB_FOLDER);
		b = file.mkdir();
		if (!b)
			return false;
		file = new File(PLUGINS_FOLDER);
		try {
			org.apache.commons.io.FileUtils.copyDirectoryToDirectory(file,
					new File(workingDir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		file = new File(workingDir + File.separator + LOGS_FOLDER);
		b = file.mkdir();
		if (!b)
			return false;
		file = new File(SETTINGSFILE);
		try {
			org.apache.commons.io.FileUtils.copyFile(file, new File(workingDir
					+ File.separator + SETTINGSFILE));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Return if two strings are similar or not. Assumes non-null check is done
	 * on args
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean isSimilar(String str1, String str2) {
		try {
			str1 = str1.trim().toLowerCase();
			str2 = str2.trim().toLowerCase();
			int thres = 3;
			if (Math.min(str1.length(), str2.length()) <= 5) {
				thres = 1;
			}
			if (editDistance(str1, str2) <= thres)
				return true;
			return false;
		} catch (Exception e) {
			return false;
		}

	}

	public static void openBrowser(String url) {

		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(url));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void copyPlugins() {
		File appDataFolder = getAppDirectoryFolder(PLUGINS_FOLDER);
		File srcPluginFolder = new File(PLUGINS_FOLDER + File.separator
				+ EXTRACTORS_FOLDER);
		File destPluginFolder = new File(appDataFolder.getAbsolutePath()
				+ File.separator + EXTRACTORS_FOLDER);

		updatePluginType(srcPluginFolder, destPluginFolder, EXTRACTOR_JAR);

		srcPluginFolder = new File(PLUGINS_FOLDER + File.separator
				+ STRATEGIES_FOLDER);
		destPluginFolder = new File(appDataFolder.getAbsolutePath()
				+ File.separator + STRATEGIES_FOLDER);
		updatePluginType(srcPluginFolder, destPluginFolder, STRATEGY_JAR);
	}

	private static void updatePluginType(File srcFolder, File destFolder,
			String pluginJarName) {
		File appDataJarFile;
		File pluginJarFile;
		File[] plugins = srcFolder.listFiles();
		for (File plugin : plugins) {
			appDataJarFile = new File(destFolder.getAbsolutePath()
					+ File.separator + plugin.getName() + File.separator
					+ pluginJarName);
			pluginJarFile = new File(srcFolder.getAbsolutePath()
					+ File.separator + plugin.getName() + File.separator
					+ pluginJarName);
			if (!appDataJarFile.exists()) {
				// copy the entire folder
				try {
					org.apache.commons.io.FileUtils.copyDirectoryToDirectory(
							pluginJarFile.getParentFile(), destFolder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("Error copying directory: "
							+ pluginJarFile.getParentFile().getAbsolutePath(),
							e);
				}
			} else {
				// update the plugin jar
				try {
					org.apache.commons.io.FileUtils.copyFile(pluginJarFile,
							appDataJarFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(
							"Could not copy jar file: "
									+ pluginJarFile.getAbsolutePath(), e);
				}
			}
		}

	}

	/**
	 * Returns a boolean with the probability of being true being "probability"
	 * value
	 * 
	 * @param probability
	 *            probability of being true
	 * @return
	 */
	public static boolean getRandomBoolean(int probability) {
		int c = (int) (Math.random() * 100);
		return c >= (100 - probability);
	}

	public static Song getRandomPick(Map<Song, Double> songWeights,
			double random) {
		double cnt = 0d;
		Iterator<Entry<Song, Double>> it = songWeights.entrySet().iterator();
		Entry<Song, Double> entry;
		while (it.hasNext()) {
			entry = it.next();
			cnt += entry.getValue();
			if (cnt >= random)
				return entry.getKey();
		}
		return null;
	}
}
