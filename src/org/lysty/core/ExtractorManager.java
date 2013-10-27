package org.lysty.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;

import org.apache.log4j.Logger;
import org.lysty.db.DBHandler;
import org.lysty.extractors.FeatureExtractor;

public class ExtractorManager {

	private static final String EXTRACTOR_JAR_NAME = "extractor.jar";
	private static List<FeatureExtractor> extractors;
	private static Map<FeatureExtractor, Long> extractorTimestamp;
	static Logger logger = Logger.getLogger(ExtractorManager.class);

	/**
	 * @return the extractors
	 */
	public static List<FeatureExtractor> getExtractors() {
		return extractors;
	}

	public static Long getExtractorTimestamp(FeatureExtractor extractor) {
		return extractorTimestamp.get(extractor);
	}

	public static void loadExtractors() {
		PluginManager manager = PluginManagerFactory.createPluginManager();
		extractors = new ArrayList<FeatureExtractor>();
		extractorTimestamp = new HashMap<FeatureExtractor, Long>();
		File file = new File(
				PropertyManager
						.getProperty(PropertyManager.EXTRACTOR_FOLDER_PATH));
		File[] sDirs = file.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		File jarFile;
		for (File sDir : sDirs) {
			try {
				manager = PluginManagerFactory.createPluginManager();
				jarFile = new File(sDir.getAbsolutePath() + File.separator
						+ EXTRACTOR_JAR_NAME);
				manager.addPluginsFrom(jarFile.toURI());
				FeatureExtractor extractor = manager
						.getPlugin(FeatureExtractor.class);
				if (extractor != null) {
					extractors.add(extractor);
					logger.info("Loaded extractor: " + sDir.getName());
					Long timestamp = DBHandler.getInstance()
							.getSetExtractorTimestamp(extractor);
					extractorTimestamp.put(extractor, timestamp);
				}
			} catch (Exception e) {
				logger.error("Failed to load extractor: " + sDir.getName(), e);
			}

		}
	}

	public static FeatureExtractor getExtractor(String className) {
		for (FeatureExtractor extractor : extractors) {
			if (extractor.getClass().getName().equalsIgnoreCase(className))
				return extractor;
		}
		return null;
	}

	public static String[] getSupportedFormats() {
		Set<String> set = new HashSet<String>();
		for (FeatureExtractor extractor : extractors) {
			set.addAll(extractor.getSupportedFileFormats());
		}
		return set.toArray(new String[set.size()]);
	}

}
