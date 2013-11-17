package org.lysty.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

public class AppSettingsManager {

	public static final String PLAY_NEXT_WHEN_LOAD_ON_CURRENT_PLAY = "play_next_when_load_on_current_play";

	public static final String REM_UNPLAYED_AUTOGENS_ON_MANUAL_ADD = "rem_unplayed_autogens_on_manual_add";

	public static final String LS_INFINI_PLAY = "ls_infini_play"; // last state
																	// - infini
																	// play

	public static final String INFINIPLAY_GENLIST_SIZE = "infiniplay_genlist_size";

	public static final String INFINIPLAY_LAST_N_TO_CHECK = "infiniplay_last_n_to_check";

	public static final String LS_PPL_SIZE = "ls_ppl_size"; // last created
															// partial playlist
															// size

	public static final String LS_FILL_STRATEGY = "ls_fill_strategy";

	public static final String LS_IS_CIRC_PPL = "ls_is_circ_ppl"; // last
																	// playlist
																	// was
																	// cirular
																	// or not

	public static final String LS_VOLUME_LEVEL = "ls_volume_level";

	public static final String LS_X = "ls_x";
	public static final String LS_Y = "ls_y";

	static Properties properties;
	private static File file;
	private static Logger logger = Logger.getLogger(AppSettingsManager.class);

	public static void loadProperties(File file) {
		AppSettingsManager.file = file;
		properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (Exception e) {
			logger.error("Error loading app settings", e);
		}
	}

	public static void setProperty(String key, String value) {
		properties.put(key, value);
	}

	public static String getProperty(String key) {
		return (String) properties.get(key);
	}

	public static String getProperty(String key, String defaultValue) {
		String ret = getProperty(key);
		if (ret != null)
			return ret;
		return defaultValue;
	}

	public static boolean getPropertyAsBoolean(String key) {
		return "true".equalsIgnoreCase((String) properties.get(key));
	}

	public static void writeAppSettings() {
		Writer writer;
		try {
			writer = new PrintWriter(file);
			properties.store(writer, "");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			logger.error("Error writing app settins", e);
		}
	}

	public static void updateAppSettings(Map<String, String> settings) {
		Iterator<Entry<String, String>> it = settings.entrySet().iterator();
		Entry<String, String> entry;
		while (it.hasNext()) {
			entry = it.next();
			properties.put(entry.getKey(), entry.getValue());
		}
	}
}
