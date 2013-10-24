package org.lysty.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;

import org.lysty.core.XMLSerializable;
import org.lysty.db.DBHandler;
import org.lysty.exceptions.InvalidXMLException;

/**
 * Selection profile of songs
 * 
 * @author NuwanSam
 * 
 */
public class SongSelectionProfile implements XMLSerializable {

	private static final String XML_SEL_PROFILE = "selection_profile";
	private static final String XML_ATTRIB_SELPROF_SIZE = "size";
	private static final String XML_ATTRIB_SELPROF_SIZETYPE = "sizetype";
	private static final String XML_SONG = "song";
	private static final String XML_ATTRIB_SONG_ID = "id";
	private static final String XML_ATTRIB_SONG_FILEPATH = "file";
	private static final String XML_ATTRIB_SONG_RELPOS = "relpos";

	public static final int SIZE_TYPE_TIME = 1;
	public static final int SIZE_TYPE_LENGTH = 0;

	String name;
	int size;
	int sizeType;
	File file; // save location
	Map<Song, Integer> relPosMap; // relative position of each song in the list;

	public SongSelectionProfile() {
		relPosMap = new HashMap<Song, Integer>();
	}

	public SongSelectionProfile(File file) {
		setFile(file);
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

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the sizeType
	 */
	public int getSizeType() {
		return sizeType;
	}

	/**
	 * @param sizeType
	 *            the sizeType to set
	 */
	public void setSizeType(int sizeType) {
		this.sizeType = sizeType;
	}

	/**
	 * @return the relPosMap
	 */
	public Map<Song, Integer> getRelPosMap() {
		return relPosMap;
	}

	/**
	 * @param relPosMap
	 *            the relPosMap to set
	 */
	public void setRelPosMap(Map<Song, Integer> relPosMap) {
		this.relPosMap = relPosMap;
	}

	public void updateRelPosMap(List<Song> songs) {
		if (sizeType == SIZE_TYPE_LENGTH)
			size = songs.size();
		relPosMap.clear();
		for (int i = 0; i < songs.size(); i++) {
			if (songs.get(i) != null) {
				relPosMap.put(songs.get(i), i);
			}
		}
	}

	@Override
	public IXMLElement getXml() {
		XMLElement xmlSelProf = new XMLElement();
		xmlSelProf.setName(XML_SEL_PROFILE);
		xmlSelProf.setAttribute(XML_ATTRIB_SELPROF_SIZE, getSize() + "");
		xmlSelProf
				.setAttribute(XML_ATTRIB_SELPROF_SIZETYPE, getSizeType() + "");
		Map<Song, Integer> songs = getRelPosMap();
		Iterator<Entry<Song, Integer>> it = songs.entrySet().iterator();
		Entry<Song, Integer> entry;
		XMLElement xmlSong;
		while (it.hasNext()) {
			entry = it.next();
			xmlSong = new XMLElement();
			xmlSong.setName(XML_SONG);
			xmlSong.setAttribute(XML_ATTRIB_SONG_ID, entry.getKey().getId()
					+ "");
			xmlSong.setAttribute(XML_ATTRIB_SONG_FILEPATH, entry.getKey()
					.getFilepath());
			xmlSong.setAttribute(XML_ATTRIB_SONG_RELPOS, entry.getValue() + "");
			xmlSelProf.addChild(xmlSong);
		}
		// TODO Auto-generated method stub
		return xmlSelProf;
	}

	@Override
	public void loadFromXml(IXMLElement xmlElement) throws InvalidXMLException {
		try {
			setSize(Integer.parseInt(xmlElement.getAttribute(
					XML_ATTRIB_SELPROF_SIZE, null)));
			setSizeType(Integer.parseInt(xmlElement.getAttribute(
					XML_ATTRIB_SELPROF_SIZETYPE, null)));
			Enumeration<IXMLElement> e = xmlElement.enumerateChildren();
			IXMLElement songElement;
			Song song;
			int relPos;
			while (e.hasMoreElements()) {
				songElement = e.nextElement();
				song = DBHandler.getInstance().getSong(
						new File(songElement.getAttribute(
								XML_ATTRIB_SONG_FILEPATH, null)));
				relPos = Integer.parseInt(songElement.getAttribute(
						XML_ATTRIB_SONG_RELPOS, null));
				relPosMap.put(song, relPos);
			}
		} catch (Exception e) {
			throw new InvalidXMLException();
		}
	}

	public List<Song> getPartialPlaylist() {
		List<Song> playlist = new ArrayList<Song>(size);
		for (int i = 0; i < size; i++) {
			playlist.add(null);
		}
		Iterator<Entry<Song, Integer>> it = relPosMap.entrySet().iterator();
		Entry<Song, Integer> entry;
		while (it.hasNext()) {
			entry = it.next();
			playlist.set(entry.getValue(), entry.getKey());
		}
		return playlist;
	}

}
