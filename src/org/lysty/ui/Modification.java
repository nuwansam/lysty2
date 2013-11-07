package org.lysty.ui;

import org.lysty.dao.Song;

public class Modification {
	/**
	 * abstraction for a modification that needs to be pushed to the db that
	 * could be a feature value update of an existing song, or an addition of a
	 * song. In the latter case only the file argument will be provided.
	 * 
	 * @param file
	 * @param feature
	 * @param newValue
	 * @param oldValue
	 */
	public Modification(Song file, String feature, String newValue,
			String oldValue) {
		this.song = file;
		this.feature = feature;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	public Modification(Song song) {
		this.song = song;
	}

	public final Song getSong() {
		return song;
	}

	public final String getFeature() {
		return feature;
	}

	public final String getNewValue() {
		return newValue;
	}

	public final String getOldValue() {
		return oldValue;
	}

	private Song song;
	private String feature;
	private String newValue;
	private String oldValue;
}
