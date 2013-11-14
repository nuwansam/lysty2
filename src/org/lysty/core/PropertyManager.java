package org.lysty.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertyManager {

	public static final String DEF_PLAYLIST_LEN = "def_playlist_len";
	public static final String DB_FOLDER = "db_dir";
	public static final String LOGS_FOLDER = "logs_dir";
	public static final String SETTINGS_FILE = "settings_file";
	public static final String SQLS_DIR = "sqls_dir";
	public static final String PLUGINS_DIR = "plugins_dir";
	static Properties properties;

	public static void loadProperties() {
		File file = new File("config/config.properties");

		properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setProperty(String key, String value) {
		properties.put(key, value);
	}

	public static String getProperty(String key) {
		return (String) properties.get(key);
	}
}
