package org.lysty.dao;

import org.lysty.db.DBHandler;

public class HistoryStatRecord {

	Song song;
	Long songId;
	Long completedCount;
	Long uncompletedCount;

	public HistoryStatRecord(Long songId, Long completedCount,
			Long uncompletedCount) {
		this.songId = songId;
		this.song = DBHandler.getInstance().getSong(songId);
		this.completedCount = completedCount;
		this.uncompletedCount = uncompletedCount;
	}

	public HistoryStatRecord() {

	}

	public Song getSong() {
		return song;
	}

	public void setSong(Song song) {
		this.song = song;
	}

	public Long getSongId() {
		return songId;
	}

	public void setSongId(Long songId) {
		this.songId = songId;
	}

	public Long getCompletedCount() {
		return completedCount;
	}

	public void setCompletedCount(Long completedCount) {
		this.completedCount = completedCount;
	}

	public Long getUncompletedCount() {
		return uncompletedCount;
	}

	public void setUncompletedCount(Long uncompletedCount) {
		this.uncompletedCount = uncompletedCount;
	}

}
