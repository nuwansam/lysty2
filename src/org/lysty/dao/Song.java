package org.lysty.dao;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.lysty.db.DBHandler;
import org.lysty.util.FileUtils;

/**
 * Abstraction of a song
 * 
 * @author NuwanSam
 * 
 */
public class Song {

	long id;
	String name;
	File file;
	Map<String, String> attributes = new HashMap<String, String>();

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

}
