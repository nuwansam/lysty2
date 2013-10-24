package org.lysty.core;

import jAudioFeatureExtractor.DataModel;
import jAudioFeatureExtractor.ModelListener;
import jAudioFeatureExtractor.ACE.DataTypes.Batch;
import jAudioFeatureExtractor.AudioFeatures.FeatureExtractor;
import jAudioFeatureExtractor.DataTypes.RecordingInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.lysty.dao.Song;
import org.lysty.db.DBHandler;
import org.lysty.ui.PlaylistProfileWindow;
import org.vamp_plugins.Plugin;
import org.vamp_plugins.PluginLoader;
import org.vamp_plugins.PluginLoader.LoadFailedException;

public class Main {

	private static final String PATH_TO_PROPERTIES_FILE = "/config.properties";

	static Logger logger = Logger.getLogger(Main.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DOMConfigurator.configure("config/log4j_config.xml");
		PropertyManager.loadProperties(PATH_TO_PROPERTIES_FILE);
		StrategyFactory.loadStrategies();
		logger.info("strategies loaded.");
		ExtractorManager.loadExtractors();
		logger.info("extractors loaded.");
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

	public void printSongDetails(File file) {
		Song song = DBHandler.getInstance().getSong(file);
		Iterator<Entry<String, String>> it = song.getAttributes().entrySet()
				.iterator();
		Entry<String, String> entry;
		while (it.hasNext()) {
			entry = it.next();
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}

	}

	public static void method2() {
		PluginLoader loader = PluginLoader.getInstance();
		String[] list = loader.listPlugins();
		for (String plugin : list) {
			System.out.println(plugin);
		}
		try {
			Plugin plugin = loader.loadPlugin("vamp-libxtract:mean", 44100,
					PluginLoader.AdapterFlags.ADAPT_ALL);

		} catch (LoadFailedException e) {
		}

	}

	public static void method() {
		Batch b = new Batch();
		HashMap<String, Boolean> activated = new HashMap<String, Boolean>();
		activated.put("Beat Sum", true);
		b.setFeatures(activated, new HashMap<String, String[]>());

		RecordingInfo rec1 = new RecordingInfo("D:\\songs\\beautiful.mp3");
		b.setRecording(new RecordingInfo[] { rec1 });
		b.setDestination("c:\\software\\fk.txt", "c:\\software\\fv.txt");

		DataModel model = new DataModel("c:\\Software\\features.xml",

		new ModelListener() {

			@Override
			public void updateTable() {
				// TODO Auto-generated method stub

			}
		});
		FeatureExtractor[] fe = model.features;
		for (FeatureExtractor f : fe) {
			System.out.println(f.getFeatureDefinition().name + " : "
					+ f.getFeatureDefinition().description);
		}
		b.setDataModel(model);
		try {
			b.execute();

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}
}
