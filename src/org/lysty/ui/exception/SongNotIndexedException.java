package org.lysty.ui.exception;

import java.io.File;

public class SongNotIndexedException extends Exception {

	File file;

	public SongNotIndexedException(File file) {
		this.file = file;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

}
