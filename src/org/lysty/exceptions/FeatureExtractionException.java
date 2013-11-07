package org.lysty.exceptions;

import java.io.File;

public class FeatureExtractionException extends Exception {

	File file;
	private Exception exception;

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

	public void setExcepion(Exception e) {
		this.exception = e;
	}

}
