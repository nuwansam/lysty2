package org.lysty.core;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PropertyManager {

	private static Map<String, String> prop = new HashMap<String, String>();

	public static final String STRATEGY_FOLDER_PATH = "strategy_folder_path";
	public static final String DEF_PLAYLIST_LEN = "def_playlist_len";

	public static final String EXTRACTOR_FOLDER_PATH = "extractor_folder_path";

	public static final String SQLS_DIR = "sqls_dir";

	public static void loadProperties(String pathToPropertiesFile) {
		ResourceBundle rb = ResourceBundle.getBundle("org.lysty.core.config");
		Enumeration<String> keys = rb.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = rb.getString(key);
			prop.put(key, value);
		}
	}

	public static void setProperty(String key, String value) {
		prop.put(key, value);
	}

	public static String getProperty(String key) {
		return prop.get(key);
	}
}
