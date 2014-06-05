package org.lysty.dao;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.lysty.db.DBHandler;
import org.lysty.util.FileUtils;
import org.lysty.util.Utils;

/**
 * Abstraction of a song
 * 
 * @author NuwanSam
 * 
 */
public class Song {

	long id;
	String name;
	String path;
	File file;
	Map<String, String> attributes = new HashMap<String, String>();

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Song(File file) {
		setFile(file);
	}

	public Song() {
		// TODO Auto-generated constructor stub
	}

	public String getFileType() {
		return FileUtils.getFileType(file);
	}

	public String getFilepath() {
		return file.getPath();
	}

	public void loadAttributes() {
		DBHandler.getInstance().fillAttributes(this);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
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
		setName(file.getName());
	}

	/**
	 * @return the attributes
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Song other = (Song) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file)) {
			return false;
		}
		return true;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(String attrib, String value) {
		attributes.put(attrib, value);
	}

	public String getAttribute(String attrib) {
		return attributes.get(attrib);
	}

	public void setAttribute(String feature, String value) {
		attributes.put(feature, value);
	}
}
