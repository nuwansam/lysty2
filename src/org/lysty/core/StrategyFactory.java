package org.lysty.core;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.n3.nanoxml.IXMLElement;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;

import org.apache.log4j.Logger;
import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.db.DBHandler;
import org.lysty.exceptions.InvalidXMLException;
import org.lysty.exceptions.StrategyInitiationException;
import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.StrategyConfiguration;
import org.lysty.util.FileUtils;

import christophedelory.plist.Array;

public class StrategyFactory {

	private static final String SETTINGS_FRAME_CLASSNAME_SUFFIX = "SettingsFrame";
	private static final String STRATEGY_JAR_NAME = "strategy.jar";
	private static final String STRATEGY_FOLDER_NAME = "strategy";
	private static List<PlaylistGenerator> allStrategies;
	private static Logger logger = Logger.getLogger(StrategyFactory.class);

	public static List<Song> getPlaylistByStrategy(PlaylistGenerator strategy,
			SongSelectionProfile profile, StrategyConfiguration config)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, StrategyInitiationException {

		List<Song> playlist = strategy.getPlaylist(profile, config);
		return cleanPlaylist(playlist);
	}

	private static void fillSongAttributes(SongSelectionProfile profile) {
		Iterator<Entry<Song, Integer>> it = profile.getRelPosMap().entrySet()
				.iterator();
		Entry<Song, Integer> entry;
		while (it.hasNext()) {
			entry = it.next();
			DBHandler.getInstance().fillAttributes(entry.getKey());
		}
	}

	private static List<Song> cleanPlaylist(List<Song> playlist) {
		List<Song> cleanList = new ArrayList<Song>();
		for (int i = 0; i < playlist.size(); i++) {
			if (playlist.get(i) != null) {
				cleanList.add(playlist.get(i));
			}
		}
		return cleanList;
	}

	/**
	 * return all strategies currently in the system. for now, it only passes
	 * hardcoded strategy list
	 * 
	 * @return
	 */
	public static List<PlaylistGenerator> getAllStrategies() {
		// TODO Auto-generated method stub
		return allStrategies;
	}

	/**
	 * Returns the strategy settings panel for the provided strategy
	 * 
	 * @param strategy
	 * @return
	 */
	public static AbstractStrategySettingsPanel getStrategySettingsPanel(
			PlaylistGenerator strategy) {
		try {
			AbstractStrategySettingsPanel frame = strategy
					.getStrategySettingsFrame();
			frame.createUI();
			frame.setConfig(getDefaultOrLastSettings(strategy));
			return frame;
		} catch (Exception e) {
			logger.error(
					"Error getting strategy panel for "
							+ strategy.getStrategyDisplayName(), e);
			return null;
		}
	}

	/**
	 * Returns the last saved settings for the given strategy. if not available,
	 * the default settings for the given strategy is returned
	 * 
	 * @param currentStrategy
	 * @return
	 */
	public static StrategyConfiguration getDefaultOrLastSettings(
			PlaylistGenerator strategy) {
		StrategyConfiguration lastSettings = null;
		try {
			lastSettings = getLastSettings(strategy);
		} catch (FileNotFoundException e) {
			logger.error(
					"last settings not found for: "
							+ strategy.getStrategyDisplayName(), e);
		}
		if (lastSettings == null) {
			return getDefaultSettings(strategy);
		}
		return lastSettings;
	}

	private static StrategyConfiguration getLastSettings(
			PlaylistGenerator strategy) throws FileNotFoundException {
		String path = PropertyManager
				.getProperty(PropertyManager.STRATEGY_FOLDER_PATH);
		File file = new File(path + File.separator + strategy.toString()
				+ File.separator + "lastSettings.xml");
		return getConfig(file);
	}

	private static StrategyConfiguration getConfig(File file) {
		IXMLElement xml = FileUtils.readXml(file);
		try {
			StrategyConfiguration config = new StrategyConfiguration(xml);
			return config;
		} catch (InvalidXMLException e) {
			logger.error("Error reading config: " + file.getName(), e);
		}
		return null;

	}

	public static StrategyConfiguration getDefaultSettings(
			PlaylistGenerator strategy) {
		String path = PropertyManager
				.getProperty(PropertyManager.STRATEGY_FOLDER_PATH);
		File file = new File(path + File.separator + strategy.toString()
				+ File.separator + "defSettings.xml");
		return getConfig(file);
	}

	public static void updateLastSettings(PlaylistGenerator strategy,
			StrategyConfiguration strategySettngs) {
		String path = PropertyManager
				.getProperty(PropertyManager.STRATEGY_FOLDER_PATH);
		File file = new File(path + File.separator + strategy.toString()
				+ File.separator + "lastSettings.xml");

		try {
			FileUtils.writeXml(file, strategySettngs.getXml());
		} catch (IOException e) {
			logger.error(
					"Error writing last settings for "
							+ strategy.getStrategyDisplayName(), e);
		}
	}

	public static void loadStrategies() {
		PluginManager manager = PluginManagerFactory.createPluginManager();
		allStrategies = new ArrayList<PlaylistGenerator>();
		File file = new File(
				PropertyManager
						.getProperty(PropertyManager.STRATEGY_FOLDER_PATH));
		File[] sDirs = file.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		File jarFile;
		for (File sDir : sDirs) {
			jarFile = new File(sDir.getAbsolutePath() + File.separator
					+ STRATEGY_JAR_NAME);
			manager.addPluginsFrom(jarFile.toURI());
			PlaylistGenerator strategy = manager
					.getPlugin(PlaylistGenerator.class);
			if (strategy != null) {
				allStrategies.add(strategy);
				logger.info("Loaded fill strategy: " + sDir.getName());
			}
		}
	}
}
