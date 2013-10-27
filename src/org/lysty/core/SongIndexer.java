package org.lysty.core;

import java.io.File;
import java.io.FileFilter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lysty.dao.Song;
import org.lysty.db.DBHandler;
import org.lysty.exceptions.FeatureExtractionException;
import org.lysty.extractors.ExtractSequencer;
import org.tritonus.share.TNotifier.NotifyEntry;

public class SongIndexer {

	public static final int DEFAULT_DEPTHS_TO_INDEX = 100;

	// update notification precision. 100 means every 1% completion is notified.
	private static final int INDEX_UPDATE_PERCENTAGE = 100;
	static String[] exts = null;
	static FileFilter filter = new FileFilter() {

		@Override
		public boolean accept(File file) {
			if (file.getName() == null)
				return false;
			if (file.isDirectory())
				return true;

			for (String ext : exts) {
				if (file.getName().endsWith("." + ext))
					return true;
			}
			return false;
		}
	};
	public static Map<String, Long> folderIndexMap = new HashMap<String, Long>();
	public static Map<String, Long> extractorTimestampMap = new HashMap<String, Long>();
	public static List<Song> songList = new ArrayList<Song>(); // song list to
																// index
	static File[] list; // temp var to hold file filter results;
	private static List<Song> allSongs;
	private static File currentIndexingFolder = null;
	private static Long maxExtractorTimstamp;

	private static boolean isCancelled;

	private static void createFolderIndexMap() {
		folderIndexMap = DBHandler.getInstance().getFolderIndexMap();
		extractorTimestampMap = DBHandler.getInstance()
				.getExtractorTimestampMap();
		Iterator<Long> it = extractorTimestampMap.values().iterator();
		maxExtractorTimstamp = 0l;
		while (it.hasNext()) {
			maxExtractorTimstamp = Math.max(maxExtractorTimstamp, it.next());
		}

	}

	private static void createFileList(File folder, int depths,
			boolean isIncremental) {

		list = folder.listFiles(filter);
		long id;
		if (list == null)
			return;
		Song song = null;
		Long lastIndex;
		for (File file : list) {
			if (file.isDirectory() && depths > 0) {
				createFileList(file, depths - 1, isIncremental);
			} else {
				lastIndex = folderIndexMap.get(folder.getPath());
				int songInDBIndex = allSongs.indexOf(new Song(file));
				if (songInDBIndex >= 0)
					song = allSongs.get(songInDBIndex);
				if (!isIncremental
						|| (lastIndex == null || songInDBIndex == -1 || maxExtractorTimstamp > lastIndex)) {
					if (songInDBIndex == -1) {
						song = new Song();
						song.setFile(file);
						song.setName(file.getName());
						id = DBHandler.getInstance().insertSong(song);
						song.setId(id);
					}
					songList.add(song);
				}
			}
		}
	}

	private static void extractFeatures(Song song)
			throws FeatureExtractionException {
		File parentFolder = song.getFile().getParentFile();
		if (currentIndexingFolder == null) {
			currentIndexingFolder = parentFolder;
		} else if (!currentIndexingFolder.equals(parentFolder)) {
			// done with current indexing folder.
			DBHandler.getInstance().setFolderIndexTimestamp(
					currentIndexingFolder, System.currentTimeMillis());
			currentIndexingFolder = parentFolder;
		}
		ExtractSequencer.getInstance().extract(song,
				folderIndexMap.get(parentFolder.getPath()));
		DBHandler.getInstance().insertAttributes(song);
	}

	public static void index(File[] folders, int depths, boolean isIncremental,
			UpdateListener updater) {
		init();
		createFolderIndexMap();
		allSongs = DBHandler.getInstance().getSongs(null);
		for (File folder : folders) {
			createFileList(folder, depths, isIncremental);
		}
		updater.setSize(songList.size());
		runExtractor(updater);
	}

	private static void init() {
		// TODO Auto-generated method stub
		isCancelled = false;
		songList = new ArrayList<Song>();
		currentIndexingFolder = null;
		exts = ExtractorManager.getSupportedFormats();
	}

	/**
	 * Runs the extractor
	 * 
	 * @param updater
	 *            updater to notify of progress of run
	 * @return the files that failed during extraction
	 */
	private static List<Song> runExtractor(UpdateListener updater) {
		List<Song> failedList = new ArrayList<Song>();
		int indexUpdateChunk = (songList.size() / INDEX_UPDATE_PERCENTAGE) + 1;

		for (int i = 0; i < songList.size(); i++) {
			try {
				if (isCancelled)
					return null;
				extractFeatures(songList.get(i));
			} catch (FeatureExtractionException e) {
				failedList.add(songList.get(i));
				updater.notifyError(e);
			}
			if (i % indexUpdateChunk == 0)
				updater.notifyUpdate(i, "Indexing: "
						+ songList.get(i).getFile().getParent());
		}
		if (!songList.isEmpty()) { // update the last folder indexed's timestamp
			currentIndexingFolder = songList.get(songList.size() - 1).getFile()
					.getParentFile();
			DBHandler.getInstance().setFolderIndexTimestamp(
					currentIndexingFolder, System.currentTimeMillis());
		}
		updater.notifyComplete();
		return failedList;
	}

	public static void cancel() {
		isCancelled = true;
	}
}
