package org.lysty.dao;

import java.util.Date;

public class HistoryEntry {

	Long songId;
	Date playTime;
	boolean isCompleted;

	public HistoryEntry() {

	}

	public HistoryEntry(Long songId, Date playTime, boolean isCompleted) {
		this.songId = songId;
		this.playTime = playTime;
		this.isCompleted = isCompleted;
	}

	public Long getSongId() {
		return songId;
	}

	public void setSongId(Long songId) {
		this.songId = songId;
	}

	public Date getPlayTime() {
		return playTime;
	}

	public void setPlayTime(Date playTime) {
		this.playTime = playTime;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

}
