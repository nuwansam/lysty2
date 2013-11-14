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

import org.apache.log4j.Logger;
import org.lysty.core.PlaylistGenerator;
import org.lysty.core.StrategyFactory;
import org.lysty.core.XMLSerializable;
import org.lysty.db.DBHandler;
import org.lysty.exceptions.InvalidXMLException;
import org.lysty.strategies.StrategyConfiguration;

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
	private static final String XML_ELEM_STRATEGY = "strategy";
	private static final String XML_ATTRIB_STRATEGY_CLASS = "class";
	private static final String XML_ELEM_STRATEGY_CONFIG = "strategy_config";
	private static final String XML_ELEM_SONGS = "songs";
	private static final String XML_ATTRIB_IS_CIRCULAR = "is_circular";

	private static Logger logger = Logger.getLogger(SongSelectionProfile.class);

	String name;
	int size;
	int sizeType;
	File file; // save location
	Map<Song, Integer> relPosMap; // relative position of each song in the list;
	StrategyConfiguration strategyConfig;
	PlaylistGenerator strategy;
	private boolean circular;

	/**
	 * @return the strategyConfig
	 */
	public StrategyConfiguration getStrategyConfig() {
		return strategyConfig;
	}

	/**
	 * @param strategyConfig
	 *            the strategyConfig to set
	 */
	public void setStrategyConfig(StrategyConfiguration strategyConfig) {
		this.strategyConfig = strategyConfig;
	}

	/**
	 * @return the strategy
	 */
	public PlaylistGenerator getStrategy() {
		return strategy;
	}

	/**
	 * @param strategy
	 *            the strategy to set
	 */
	public void setStrategy(PlaylistGenerator strategy) {
		this.strategy = strategy;
	}

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
		xmlSelProf.setAttribute(XML_ATTRIB_IS_CIRCULAR, isCircular() ? "true"
				: "false");
		Map<Song, Integer> songs = getRelPosMap();
		Iterator<Entry<Song, Integer>> it = songs.entrySet().iterator();
		Entry<Song, Integer> entry;
		XMLElement xmlSong;
		XMLElement xmlSongs = new XMLElement();
		xmlSongs.setName(XML_ELEM_SONGS);
		while (it.hasNext()) {
			entry = it.next();
			xmlSong = new XMLElement();
			xmlSong.setName(XML_SONG);
			xmlSong.setAttribute(XML_ATTRIB_SONG_ID, entry.getKey().getId()
					+ "");
			xmlSong.setAttribute(XML_ATTRIB_SONG_FILEPATH, entry.getKey()
					.getFilepath());
			xmlSong.setAttribute(XML_ATTRIB_SONG_RELPOS, entry.getValue() + "");
			xmlSongs.addChild(xmlSong);
		}
		xmlSelProf.addChild(xmlSongs);

		XMLElement xmlStrategy = new XMLElement();
		xmlStrategy.setName(XML_ELEM_STRATEGY);
		xmlStrategy.setAttribute(XML_ATTRIB_STRATEGY_CLASS, strategy.getClass()
				.getName());
		xmlStrategy.addChild(strategyConfig.getXml());
		xmlSelProf.addChild(xmlStrategy);
		return xmlSelProf;
	}

	@Override
	public void loadFromXml(IXMLElement xmlElement) throws InvalidXMLException {
		try {
			setSize(Integer.parseInt(xmlElement.getAttribute(
					XML_ATTRIB_SELPROF_SIZE, null)));
			setSizeType(Integer.parseInt(xmlElement.getAttribute(
					XML_ATTRIB_SELPROF_SIZETYPE, null)));
			setIsCircular("true".equalsIgnoreCase(xmlElement.getAttribute(
					XML_ATTRIB_IS_CIRCULAR, null)));

			IXMLElement strategyElement = (IXMLElement) xmlElement
					.getChildrenNamed(XML_ELEM_STRATEGY).get(0);
			String strategyClass = strategyElement.getAttribute(
					XML_ATTRIB_STRATEGY_CLASS, null);
			strategy = StrategyFactory.getStrategyByClassName(strategyClass);
			IXMLElement strategyConfigElement = (IXMLElement) strategyElement
					.getChildrenNamed(XML_ELEM_STRATEGY_CONFIG).get(0);
			strategyConfig = new StrategyConfiguration(strategyConfigElement);

			IXMLElement songsElement = (IXMLElement) xmlElement
					.getChildrenNamed(XML_ELEM_SONGS).get(0);
			Enumeration<IXMLElement> e = songsElement.enumerateChildren();
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
			logger.error("Error parsing partila playlist file", e);
			throw new InvalidXMLException();
		}
	}

	public void setIsCircular(boolean isCircular) {
		circular = isCircular;
	}

	public boolean isCircular() {
		return circular;
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

	public void setRelPosMap(List<Song> partials) {
		Song song;
		for (int i = 0; i < partials.size(); i++) {
			song = partials.get(i);
			if (song != null) {
				if (song.getId() == 0) {
					// song was not taken from db. try to get from db
					// happens when the indexer was run after the song was added
					// for play as a file
					Song indexedSong = DBHandler.getInstance().getSong(
							song.getFile());
					if (indexedSong != null) {
						song = indexedSong;
					}
				}
				relPosMap.put(song, i);
			}
		}
	}
}
