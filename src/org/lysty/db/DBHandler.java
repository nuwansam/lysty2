package org.lysty.db;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.ProgressMonitor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.h2.tools.RunScript;
import org.lysty.dao.Song;
import org.lysty.extractors.FeatureExtractor;
import org.lysty.ui.Modification;
import org.lysty.util.Utils;

public class DBHandler {

	private static final long INSERTION_FAIL_ID = -1;
	public static final String DB_FOLDER_PROP = "db_dir";
	private static final String SQLS_DIR = "sqls";

	public static DBHandler self = null;
	public static SqlSessionFactory sqlSessionFactory;
	private static Logger logger = Logger.getLogger(DBHandler.class);

	private DBHandler() throws IOException {
		File file = new File("config/mybatis-config.xml");
		InputStream inputStream = null;
		inputStream = new FileInputStream(file);
		Properties props = new Properties();
		props.put(DB_FOLDER_PROP, Utils.getAppDirectoryFolder(Utils.DB_FOLDER)
				.getAbsolutePath());
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream,
				props);
		try {
			updateDB();
		} catch (SQLException e) {
			logger.error("Could not update DB", e);
		}
	}

	public static void updateDB() throws FileNotFoundException, SQLException {
		int n = getLastDBUpdateScript();
		File sqlsDir = new File(SQLS_DIR);
		File[] scripts = sqlsDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if (file.getName().endsWith(".sql"))
					return true;
				return false;
			}
		});

		File[] scriptsOrdered = new File[scripts.length];
		for (File script : scripts) {
			scriptsOrdered[getScriptNumber(script)] = script;
		}

		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			Connection connection = session.getConnection();
			for (int i = n + 1; i < scriptsOrdered.length; i++) {
				logger.info("Running db script: " + scriptsOrdered[i].getName());
				RunScript
						.execute(connection, new FileReader(scriptsOrdered[i]));
				setLastRunScriptNum(i);
			}

			logger.info("DB Update complete");
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
	}

	private static void setLastRunScriptNum(int last) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			mapper.setLastDBScriptNum(last);
			session.commit();
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
	}

	private static int getScriptNumber(File script) {
		String numStr = script.getName().split("_")[0];
		return Integer.parseInt(numStr);
	}

	private static int getLastDBUpdateScript() {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		Integer last = null;
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			last = mapper.getLastDBScriptNum();
			if (last == null) {
				return -1;
			}
			return last;
		} catch (Exception e) {
			logger.error("DB Error", e);
			return -1;
		} finally {
			session.close();
		}
	}

	public static DBHandler getInstance() {
		if (self == null) {
			try {
				self = new DBHandler();
			} catch (IOException e) {
				logger.error("Error creating DBHandler", e);
			}
		}
		return self;
	}

	// DB Calls

	public long insertSong(Song song) {
		if (song.getFile().isDirectory())
			return -1;
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		Long id = INSERTION_FAIL_ID;
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			id = mapper.getSongId(song);
			if (id == null || id == 0) {
				id = mapper.insertSong(song);
				session.commit();
				id = mapper.getSongId(song);
			}
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
		return id;
	}

	public Song getSong(long id) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		Song song = null;
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			song = mapper.getSong(id);
			song.setFile(new File(song.getPath()));
			fillAttributes(song);
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
		return song;
	}

	public void insertAttributes(Song song) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			Iterator<Entry<String, String>> iter = song.getAttributes()
					.entrySet().iterator();
			Entry<String, String> entry;
			while (iter.hasNext()) {
				entry = iter.next();
				mapper.insertAttribute(song.getId(), entry.getKey(),
						entry.getValue());
			}
			session.commit();
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}

	}

	/**
	 * Fills the attributes from the db
	 * 
	 * @param song
	 */
	public void fillAttributes(Song song) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			Map<String, String> attribs = getAttribMapFromMapList(mapper
					.getAttributes(song));
			song.setAttributes(attribs);
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
	}

	private Map<String, String> getAttribMapFromMapList(
			List<Map<String, String>> listOfMap) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < listOfMap.size(); i++) {
			String key = listOfMap.get(i).get("ATTRIBUTE");
			String value = listOfMap.get(i).get("VALUE");
			map.put(key, value);
		}
		return map;
	}

	public Song getSong(File file) {
		Song song = new Song();
		song.setFile(file);
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		Long id = null;
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			id = mapper.getSongId(song);
			if (id == null) {
				return null;
			}
			song.setId(id);
			fillAttributes(song);
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
		return song;

	}

	public List<Map<String, Object>> getHistory(Date date) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			List<Map<String, Object>> list = mapper.getPlayHistory(date);
			if (list == null)
				return null;
			return list;
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
		return null;

	}

	public List<Song> getSongs(File folder) {
		Song song;
		long id = 0;
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			String path = folder == null ? "%" : folder.getPath() + "%";

			DBMapper mapper = session.getMapper(DBMapper.class);
			List<Map<String, String>> mapList = mapper.getSongs(path);
			Map<Long, Song> songMap = new HashMap<Long, Song>();
			for (Map<String, String> mapEntry : mapList) {
				try {
					id = Long.parseLong(String.valueOf(mapEntry.get("ID")));
				} catch (Exception e) {
					logger.error("DB Error", e);
				}
				song = songMap.get(id);
				if (song == null) {
					song = new Song();
					song.setId(id);
					song.setName(mapEntry.get("NAME"));
					song.setFile(new File(mapEntry.get("PATH")));
					songMap.put(id, song);
				}
				song.addAttribute(mapEntry.get("ATTRIBUTE"),
						mapEntry.get("VALUE"));
			}
			return new ArrayList<Song>(songMap.values());
		} catch (Exception e) {
			logger.error("DB Error", e);
			return null;
		} finally {
			session.close();
		}

	}

	public void setFolderIndexTimestamp(File folder, Long time) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			mapper.setFolderIndexTimestamp(folder.getPath(), time);
			session.commit();
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
	}

	private Map<String, Long> getIndexMapFromList(
			List<Map<String, String>> listOfMap, String keyAttrib,
			String valueAttrib) {
		Map<String, Long> map = new HashMap<String, Long>();
		for (int i = 0; i < listOfMap.size(); i++) {
			String key = listOfMap.get(i).get(keyAttrib);
			Long value = Long.parseLong(String.valueOf(
					listOfMap.get(i).get(valueAttrib)).toString());
			map.put(key, value);
		}
		return map;
	}

	public Map<String, Long> getFolderIndexMap() {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			Map<String, Long> map = getIndexMapFromList(
					mapper.getFolderIndexMap(), "FOLDER", "LASTINDEXTIME");
			if (map == null)
				return new HashMap<String, Long>();
			return map;
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
		return null;

	}

	public Map<String, Long> getExtractorTimestampMap() {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			Map<String, Long> map = getIndexMapFromList(
					mapper.getExtractorTimestampMap(), "NAME", "ADDEDTIME");
			if (map == null)
				return new HashMap<String, Long>();
			return map;
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
		return null;
	}

	/**
	 * Returns the timestamp the extractor was added to the system. If no record
	 * is found, a new record is added and current time is returned;
	 * 
	 * @param extractor
	 * @param currentTimeMillis
	 * @return
	 */
	public Long getSetExtractorTimestamp(FeatureExtractor extractor) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		Long timestamp = null;
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			timestamp = mapper.getExtractorTimestamp(extractor.getClass()
					.getName());
			if (timestamp == null) {
				timestamp = System.currentTimeMillis();
				mapper.insertExtractorTimestamp(extractor.getClass().getName(),
						timestamp);
				session.commit();
			}
			return timestamp;
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
		return null;
	}

	public void applyModifications(List<Modification> changes,
			ProgressMonitor listener) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		Long timestamp = null;
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			int currentProgress = 0;
			for (Modification change : changes) {
				currentProgress++;
				if (listener != null)
					listener.setProgress(currentProgress);
				try {
					if (change.getFeature() == null) {
						// song addition
						if (!change.getSong().getFile().isDirectory()) { // only
																			// add
																			// actual
																			// files
							insertSong(change.getSong());
							logger.info("Inserted song: "
									+ change.getSong().getFile());
						}
					} else {
						DBHandler.getInstance().setAttribute(change.getSong(),
								change.getFeature(), change.getNewValue());
					}
				} catch (Exception e) {
					logger.error(
							"Error applying change: set " + change.getSong()
									+ " " + change.getFeature() + " to "
									+ change.getNewValue(), e);
				}
			}
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}
	}

	private void setAttribute(Song song, String feature, String newValue) {
		if (song.getFile().isDirectory())
			return;

		if (song.getId() == 0) {
			// the song was not in the db at the time of setting the attribute
			// it is assumed that the song is inserted before setting the
			// attribute
			Song songInDb = getSong(song.getFile());
			if (songInDb == null) {
				// ideally should not reach. coded for safety
				insertSong(song);
				songInDb = getSong(song.getFile());
			}
			song = songInDb;
		}

		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		Long timestamp = null;
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			mapper.setAttribute(song.getId(), feature, newValue);
			logger.info("set " + feature + " of " + song.getFile().getName()
					+ " to " + newValue);
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}

	}

	public List<Song> getAllSongs() {
		return getSongs(null);
	}

	public void insertPlayRecord(Song song, Date time, boolean isCompleted) {
		SqlSession session = DBHandler.sqlSessionFactory.openSession();
		Long id = INSERTION_FAIL_ID;
		try {
			DBMapper mapper = session.getMapper(DBMapper.class);
			id = mapper.insertPlayRecord(song.getId(), time, isCompleted);
			session.commit();
		} catch (Exception e) {
			logger.error("DB Error", e);
		} finally {
			session.close();
		}

	}

}
