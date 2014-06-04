package org.lysty.db;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.lysty.dao.Song;

public interface DBMapper {

	long insertSong(Song song);

	Long getSongId(Song song);

	void insertAttribute(long songId, String key, String value);

	List<Map<String, String>> getAttributes(Song song);

	List<Map<String, String>> getFolderIndexMap();

	List<Map<String, Object>> getPlayHistory(Date dt);

	void setFolderIndexTimestamp(String folder, Long timestamp);

	List<Map<String, String>> getSongs(String parentFolder);

	List<Map<String, String>> getExtractorTimestampMap();

	Long getExtractorTimestamp(String name);

	void insertExtractorTimestamp(String name, long currentTimeMillis);

	Integer getLastDBScriptNum();

	void setLastDBScriptNum(int last);

	void setAttribute(long songId, String feature, String newValue);

	Long insertPlayRecord(long id, Date time, boolean isCompleted);

	Song getSong(long id);
}
