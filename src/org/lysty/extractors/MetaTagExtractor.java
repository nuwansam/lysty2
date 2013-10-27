package org.lysty.extractors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.lysty.dao.Song;
import org.lysty.exceptions.FeatureExtractionException;

@PluginImplementation
public class MetaTagExtractor implements FeatureExtractor {

	public static final String FEATURE_RELEASEDATE = "releaseDate";

	public static final String FEATURE_DURATION = "duration";

	public static final String FEATURE_AUDIOCHANNEL_TYPE = "audioChannelType";
	public static final String FEATURE_ARTIST = "artist";
	public static final String FEATURE_ALBUM = "album";
	public static final String FEATURE_CHANNELS = "channels";
	public static final String FEATURE_AUDIO_SAMPLE_RATE = "audioSampleRate";
	public static final String FEATURE_TITLE = "title";
	public static final String FEATURE_GENRE = "genre";

	private static final String FEATURE_COMPOSER = "composer";

	private static final String FEATURE_LANGUAGE = "language";

	private static final String FEATURE_BPM = "bpm";

	@Override
	public Song extract(Song song) throws FeatureExtractionException {
		File file = song.getFile();
		AudioFile audioFile;
		try {
			audioFile = AudioFileIO.read(file);
			int duration = audioFile.getAudioHeader().getTrackLength();
			song.addAttribute(FEATURE_DURATION, duration + "");
			Tag tag = audioFile.getTag();
			song.addAttribute(FEATURE_ALBUM, tag.getFirst(FieldKey.ALBUM));
			song.addAttribute(FEATURE_RELEASEDATE, tag.getFirst(FieldKey.YEAR));
			song.addAttribute(FEATURE_ARTIST, tag.getFirst(FieldKey.ARTIST));
			song.addAttribute(FEATURE_TITLE, tag.getFirst(FieldKey.TITLE));
			song.addAttribute(FEATURE_GENRE, tag.getFirst(FieldKey.GENRE));
			song.addAttribute(FEATURE_COMPOSER, tag.getFirst(FieldKey.COMPOSER));
			song.addAttribute(FEATURE_LANGUAGE, tag.getFirst(FieldKey.LANGUAGE));
			song.addAttribute(FEATURE_BPM, tag.getFirst(FieldKey.BPM));

		} catch (Exception e) {
			FeatureExtractionException e1 = new FeatureExtractionException();
			e1.setFile(file);
			throw e1;
		}
		return song;
	}

	@Override
	public List<String> getSupportedAttributes() {
		List<String> attribs = new ArrayList<String>();
		attribs.add(FEATURE_ALBUM);
		attribs.add(FEATURE_RELEASEDATE);
		attribs.add(FEATURE_ARTIST);
		attribs.add(FEATURE_TITLE);
		attribs.add(FEATURE_GENRE);
		attribs.add(FEATURE_COMPOSER);
		attribs.add(FEATURE_LANGUAGE);
		attribs.add(FEATURE_BPM);
		return attribs;
	}

	@Override
	public List<String> getSupportedFileFormats() {
		List<String> list = new ArrayList<String>();
		list.add("mp3");
		list.add("mp4");
		list.add("flac");
		list.add("wma");
		return list;
	}

}
