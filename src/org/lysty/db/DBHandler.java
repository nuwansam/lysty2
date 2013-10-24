package org.lysty.db;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.lysty.dao.Song;
import org.lysty.extractors.FeatureExtractor;

public class DBHandler {

	private static final long INSERTION_FAIL_ID = -1;
	public static DBHandler self = null;
	public static SqlSessionFactory sqlSessionFactory;
	private static Logger logger = Logger.getLogger(DBHandler.class);

	private DBHandler() throws IOException {
		String resource = "org/lysty/db/mybatis-config.xml";
		InputStream inputStream = null;
		inputStream = Resources.getResourceAsStream(resource);
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
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
		} catch (Exception e) {
			logger.error("DB Error",e);
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
			logger.error("DB Error",e);
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
			logger.error("DB Error",e);
		} finally {
			session.close();
		}
		return song;

	}

	public List<Song> getSongs(File folder) {
		List<Song> songs = new ArrayList<Song>();
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
					logger.error("DB Error",e);
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
			logger.error("DB Error",e);
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
			logger.error("DB Error",e);
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
			logger.error("DB Error",e);
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
			logger.error("DB Error",e);
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
			logger.error("DB Error",e);
		} finally {
			session.close();
		}
		return null;
	}
}
