package org.lysty.core;

import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.lysty.db.DBHandler;
import org.lysty.players.PlayerManager;
import org.lysty.ui.PlaylistProfileWindow;

public class Main {

	private static final String PATH_TO_PROPERTIES_FILE = "/config.properties";

	static Logger logger = Logger.getLogger(Main.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("config/log4j_config.xml");
		PropertyManager.loadProperties(PATH_TO_PROPERTIES_FILE);
		DBHandler.getInstance();
		StrategyFactory.loadStrategies();
		logger.info("strategies loaded.");
		ExtractorManager.loadExtractors();
		logger.info("extractors loaded.");
		PlayerManager.getInstance();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.error(e);
		}

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new PlaylistProfileWindow("Lysty");
			}
		});
	}

}
