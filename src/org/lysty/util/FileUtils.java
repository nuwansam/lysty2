package org.lysty.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import net.n3.nanoxml.XMLWriter;

import org.apache.log4j.Logger;
import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.players.PlayerManager;

import christophedelory.content.Content;
import christophedelory.playlist.Media;
import christophedelory.playlist.Playlist;
import christophedelory.playlist.Sequence;
import christophedelory.playlist.SpecificPlaylist;
import christophedelory.playlist.SpecificPlaylistFactory;
import christophedelory.playlist.SpecificPlaylistProvider;

public class FileUtils {

	private static final String FILE_TYPE_UNKNOWN = "FILE TYPE UNKNOWN";
	public static final String PARTIAL_PLAYLIST_EXT = "ppl";

	public static javax.swing.filechooser.FileFilter selProfileFileFilter = new javax.swing.filechooser.FileFilter() {

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean accept(File file) {
			return file.getName().endsWith("." + PARTIAL_PLAYLIST_EXT);
		}
	};
	private static Logger logger = Logger.getLogger(FileUtils.class);

	public static SongSelectionProfile loadSelectionProfile(File file) {
		SongSelectionProfile profile = new SongSelectionProfile();
		IXMLParser parser;
		try {
			parser = XMLParserFactory.createDefaultXMLParser();
			Scanner scan = new Scanner(file);
			StringBuilder builder = new StringBuilder();
			while (scan.hasNextLine()) {
				builder.append(scan.nextLine());
			}
			IXMLReader reader = StdXMLReader.stringReader(builder.toString());
			parser.setReader(reader);
			IXMLElement xml = (IXMLElement) parser.parse();
			profile.loadFromXml(xml);
			profile.setFile(file);
		} catch (Exception e) {
			logger.error(
					"Error loading selection profile from "
							+ file.getAbsolutePath(), e);
		}
		return profile;
	}

	/**
	 * Saves the selection profile to the given location
	 * 
	 * @param profile
	 * @param file
	 * @return success
	 * @throws IOException
	 */
	public static boolean saveSelectionProfile(SongSelectionProfile profile,
			File file) throws IOException {
		IXMLElement xmlSelProf = profile.getXml();
		FileWriter fWriter = new FileWriter(file);
		XMLWriter writer = new XMLWriter(fWriter);
		writer.write(xmlSelProf);
		fWriter.close();
		return false;
	}

	public static boolean savePlaylist(List<Song> list, File file)
			throws IOException {
		Playlist playlist = new Playlist();
		Sequence seq = playlist.getRootSequence();
		Media media;
		for (Song song : list) {
			media = new Media();
			media.setRepeatCount(1);
			media.setSource(new Content(song.getFile().getAbsolutePath()));
			seq.addComponent(media);
		}
		SpecificPlaylistProvider playlistProvider = SpecificPlaylistFactory
				.getInstance().findProviderByExtension(
						"." + FileUtils.getFileType(file));
		try {
			SpecificPlaylist spList = playlistProvider
					.toSpecificPlaylist(playlist);
			BufferedOutputStream bufStream = new BufferedOutputStream(
					new FileOutputStream(file));
			spList.writeTo(bufStream, null);
			bufStream.flush();
		} catch (Exception e) {
			logger.error("Error saving playlist to: " + file.getAbsolutePath(),
					e);
		}
		return false;
	}

	public static String getFileType(File file) {
		String name = file.getName();

		int i = name.lastIndexOf(".") + 1;
		if (i == 0)
			return FILE_TYPE_UNKNOWN;
		return name.substring(i);

	}

	public static IXMLElement readXml(File file) {
		IXMLParser parser;
		try {
			parser = XMLParserFactory.createDefaultXMLParser();
			Scanner scan = new Scanner(file);
			StringBuilder builder = new StringBuilder();
			while (scan.hasNextLine()) {
				builder.append(scan.nextLine());
			}
			IXMLReader reader = StdXMLReader.stringReader(builder.toString());
			parser.setReader(reader);
			IXMLElement xml = (IXMLElement) parser.parse();
			return xml;
		} catch (Exception e) {
			logger.error("Error reading xml from " + file.getAbsolutePath(), e);
		}
		return null;

	}

	public static void writeXml(File file, IXMLElement xml) throws IOException {
		FileWriter fWriter = new FileWriter(file);
		XMLWriter writer = new XMLWriter(fWriter);
		writer.write(xml);
		fWriter.close();
	}

	public static boolean isPartialPlaylistFile(File file) {
		return PARTIAL_PLAYLIST_EXT.equalsIgnoreCase(getFileType(file));
	}

	public static boolean isSupportedSongFile(File file) {
		Set<String> set = PlayerManager.getInstance().getSupportedFormats();
		if (set.contains(getFileType(file).toLowerCase())) {
			return true;
		}
		return false;
	}
}
