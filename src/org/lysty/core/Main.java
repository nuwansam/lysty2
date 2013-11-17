package org.lysty.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.lysty.dao.Song;
import org.lysty.db.DBHandler;
import org.lysty.players.PlayerManager;
import org.lysty.ui.ApplicationInstanceListener;
import org.lysty.ui.ApplicationInstanceManager;
import org.lysty.ui.PlayerPanel;
import org.lysty.ui.PlaylistPreviewWindow;
import org.lysty.ui.PlaylistProfileWindow;
import org.lysty.util.FileUtils;
import org.lysty.util.Utils;

public class Main {

	private static final String ENQUEUE = "ENQUEUE";

	private static final String PLAY_NEXT = "PLAY_NEXT";

	static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) {
		PropertyManager.loadProperties();
		System.setProperty("lysty_logs_folder_path",
				PropertyManager.getProperty(PropertyManager.LOGS_FOLDER));
		DOMConfigurator.configure("config/log4j_config.xml");
		boolean success = ApplicationInstanceManager.registerInstance(args);
		if (!success) {
			// instance already running.
			System.exit(0);
		} else {
			// first instance;
			init();
			handleArgs(args);
		}

		ApplicationInstanceManager
				.setApplicationInstanceListener(new ApplicationInstanceListener() {
					@Override
					public void newInstanceCreated(String[] args) {
						logger.info("New instance detected with following args:");
						for (int i = 0; i < args.length; i++) {
							logger.info("arg " + i + " " + args[i]);
						}
						handleArgs(args);
					}
				});
	}

	private static void init() {
		// PropertyManager.loadProperties();
		DBHandler.getInstance();
		ExtractorManager.loadExtractors();
		logger.info("extractors loaded.");
		StrategyFactory.loadStrategies();
		logger.info("strategies loaded.");
		PlayerManager.getInstance();
		AppSettingsManager.loadProperties(new File(PropertyManager
				.getProperty(PropertyManager.SETTINGS_FILE)));
		try {
			UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				logger.error(e1);
			}
			logger.error(e);
		}
	}

	/**
	 * @param args
	 */
	public static void handleArgs(String[] args) {

		if (args.length == 2) {
			// command and a file
			String command = args[0];
			File file = new File(args[1]);
			Song song = DBHandler.getInstance().getSong(file);
			if (song == null) {
				song = new Song(file);
			}
			if (PLAY_NEXT.equalsIgnoreCase(command)) {
				PlaylistPreviewWindow.getInstance().addSongNext(song);
			} else if (ENQUEUE.equalsIgnoreCase(command)) {
				PlaylistPreviewWindow.getInstance().enqueueSong(song);
			} else {
				logger.error("Invalid args passed: "
						+ Utils.getArgsLogString(args));
				return;
			}
		} else if (args.length == 1) {
			// this is a file click. do as needed.
			final File file = new File(args[0]);
			if (FileUtils.isPartialPlaylistFile(file)) {
				// partial playlist
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						PlaylistPreviewWindow.getInstance();
						PlaylistProfileWindow window = PlaylistProfileWindow
								.getInstance();
						window.loadSelProfile(file);
						logger.info("Loading partial playlist from: "
								+ file.getAbsolutePath());
					}
				});
			} else if (FileUtils.isSupportedSongFile(file)) {
				PlaylistPreviewWindow win = PlaylistPreviewWindow.getInstance();
				Song song = DBHandler.getInstance().getSong(file);
				if (song == null) {
					song = new Song(file);
				}
				if (win.getCurrentState() == PlayerPanel.PlayState.PLAYING
						&& "true"
								.equalsIgnoreCase(AppSettingsManager
										.getProperty(AppSettingsManager.PLAY_NEXT_WHEN_LOAD_ON_CURRENT_PLAY))) {
					PlaylistPreviewWindow.getInstance().addSongNext(song);
				} else {
					// SongSelectionProfile profile = new
					// SongSelectionProfile();
					Map<Song, Integer> map = new HashMap<Song, Integer>();
					map.put(song, 0);
					// profile.setRelPosMap(map);

					List<Song> songList = new ArrayList<Song>();
					songList.add(song);
					win.init(songList, true, null);
				}
			} else if (FileUtils.isSupportedPlaylistFile(file)) {
				PlaylistPreviewWindow win = PlaylistPreviewWindow.getInstance();
				win.init(FileUtils.loadPlaylist(file), true, null);
			} else {
				// unsupported file format
				logger.info("Unsupported File: " + file.getAbsolutePath());
				JOptionPane
						.showMessageDialog(
								null,
								file.getName()
										+ " cannot be played since the file type is not supported");
			}
		} else if (args.length == 0) {
			// just open the profile window
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					PlaylistPreviewWindow.getInstance();
				}
			});
		}
	}
}
