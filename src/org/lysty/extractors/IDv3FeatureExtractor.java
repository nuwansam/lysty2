package org.lysty.extractors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Executor;

import javax.swing.ComboBoxModel;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.lysty.dao.Song;
import org.lysty.exceptions.FeatureExtractionException;
import org.lysty.util.Formats;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

@PluginImplementation
public class IDv3FeatureExtractor implements FeatureExtractor {

	public static final String XMPDM_RELEASEDATE = "xmpDM:releaseDate";
	public static final String FEATURE_RELEASEDATE = "releaseDate";

	public static final String XMPDM_DURATION = "xmpDM:duration";
	public static final String FEATURE_DURATION = "duration";

	public static final String XMPDM_AUDIOCHANNEL_TYPE = "xmpDM:audioChannelType";
	public static final String FEATURE_AUDIOCHANNEL_TYPE = "audioChannelType";
	public static final String DC_CREATOR = "dc:creator";
	public static final String FEATURE_ARTIST = "artist";
	public static final String AUTHOR = "author";
	public static final String XMPDM_ARTIST = "xmpDM:artist";
	public static final String CREATOR = "creator";
	public static final String META_AUTHOR = "meta:author";
	public static final String XMPDM_ALBUM = "xmpDM:album";
	public static final String FEATURE_ALBUM = "album";
	public static final String FEATURE_CHANNELS = "channels";
	public static final String XMPDM_AUDIO_SAMPLE_RATE = "xmpDM:audioSampleRate";
	public static final String FEATURE_AUDIO_SAMPLE_RATE = "audioSampleRate";
	public static final String SAMPLERATE = "samplerate";
	public static final String FEATURE_TITLE = "title";
	public static final String DC_TITLE = "dc:title";
	public static final String XMPDM_GENRE = "xmpDM:genre";
	public static final String FEATURE_GENRE = "genre";

	static Mp3Parser mp3Parser = new Mp3Parser();
	static ContentHandler cHandler = new DefaultHandler();
	ParseContext context;
	Metadata metadata;
	InputStream stream;
	private Logger logger = Logger.getLogger(IDv3FeatureExtractor.class);

	public Song extract(Song song) throws FeatureExtractionException {
		String type = song.getFileType();
		if (Formats.MP3.equalsIgnoreCase(type)) {

			stream = null;
			try {
				stream = new FileInputStream(song.getFile());
				context = new ParseContext();
				metadata = new Metadata();
				mp3Parser.parse(stream, cHandler, metadata, context);
				populateMetaData(song, metadata);
			} catch (UnsupportedEncodingException ex) {
				logger.error("Could not extract song due to extended header: "
						+ song.getFilepath(), ex);
			} catch (Exception e) {
				logger.error("Could not extract song: " + song.getFilepath(), e);

			} finally {
				try {
					stream.close();
					context = null;
					metadata = null;
				} catch (IOException e) {
					FeatureExtractionException ex = new FeatureExtractionException();
					ex.setFile(song.getFile());
					throw ex;
				}
			}
		}
		return song;
	}

	private void populateMetaData(Song song, Metadata metadata) {
		String value;

		value = metadata.get(XMPDM_RELEASEDATE);
		if (value != null)
			song.addAttribute(FEATURE_RELEASEDATE, value);

		value = metadata.get(XMPDM_DURATION);
		if (value != null)
			song.addAttribute(FEATURE_DURATION, value);

		value = metadata.get(XMPDM_AUDIOCHANNEL_TYPE);
		if (value != null)
			song.addAttribute(FEATURE_AUDIOCHANNEL_TYPE, value);

		value = metadata.get(DC_CREATOR);
		if (value != null) {
			song.addAttribute(FEATURE_ARTIST, value);
		} else {
			value = metadata.get(AUTHOR);
			if (value != null) {
				song.addAttribute(FEATURE_ARTIST, value);
			} else {
				value = metadata.get(XMPDM_ARTIST);
				if (value != null) {
					song.addAttribute(FEATURE_ARTIST, value);
				} else {
					value = metadata.get(CREATOR);
					if (value != null) {
						song.addAttribute(FEATURE_ARTIST, value);
					} else {
						value = metadata.get(META_AUTHOR);
						if (value != null)
							song.addAttribute(FEATURE_ARTIST, value);
					}
				}
			}
		}

		value = metadata.get(XMPDM_ALBUM);
		if (value != null)
			song.addAttribute(FEATURE_ALBUM, value);

		value = metadata.get(FEATURE_CHANNELS);
		if (value != null)
			song.addAttribute(FEATURE_CHANNELS, value);

		value = metadata.get(XMPDM_AUDIO_SAMPLE_RATE);
		if (value != null) {
			song.addAttribute(FEATURE_AUDIO_SAMPLE_RATE, value);
		} else {
			value = metadata.get(SAMPLERATE);
			if (value != null)
				song.addAttribute(FEATURE_AUDIO_SAMPLE_RATE, value);
		}

		value = metadata.get(FEATURE_TITLE);
		if (value != null) {
			song.addAttribute(FEATURE_TITLE, value);
		} else {
			value = metadata.get(DC_TITLE);
			if (value != null)
				song.addAttribute(FEATURE_TITLE, value);
		}

		value = metadata.get(XMPDM_GENRE);
		if (value != null)
			song.addAttribute(FEATURE_GENRE, value);
	}

	public List<String> getSupportedAttributes() {
		return null;
	}

	public static String[] getSupportedFeatures() {
		// TODO Auto-generated method stub
		return new String[] { FEATURE_ALBUM, FEATURE_ARTIST, FEATURE_GENRE,
				FEATURE_RELEASEDATE };
	}

}
