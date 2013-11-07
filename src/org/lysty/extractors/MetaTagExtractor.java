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
import org.lysty.util.Utils;

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

	private static final String FEATURE_KEY = "key";

	private static final String FEATURE_LYRICIST = "lyricist";

	private static final String FEATURE_MOOD = "mood";

	private static final String FEATURE_ISCOMPILATION = "is_compilation";

	public static int cnt = 0;

	@Override
	public Song extract(Song song) throws FeatureExtractionException {
		File file = song.getFile();
		AudioFile audioFile;
		try {
			audioFile = AudioFileIO.read(file);
			int duration = audioFile.getAudioHeader().getTrackLength();
			song.addAttribute(FEATURE_DURATION, duration + "");
			Tag tag = audioFile.getTag();

			if (tag == null) {
				System.out.println("No tags for: " + file.getName());
				cnt++;
			}
			if (tag != null) {

				String attrib;
				try {
					attrib = tag.getFirst(FieldKey.ALBUM);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_ALBUM, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.YEAR);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_RELEASEDATE, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.ARTIST);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_ARTIST, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.TITLE);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_TITLE, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.GENRE);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_GENRE, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.COMPOSER);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_COMPOSER, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.LANGUAGE);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_LANGUAGE, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.BPM);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_BPM, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.KEY);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_KEY, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.LYRICIST);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_LYRICIST, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.MOOD);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_MOOD, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					attrib = tag.getFirst(FieldKey.IS_COMPILATION);
					if (Utils.stringNotNullOrEmpty(attrib))
						song.addAttribute(FEATURE_ISCOMPILATION, attrib);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (!song.getAttributes().keySet().iterator().hasNext()) {
				System.out.println("No attributes extracted for: "
						+ song.getFile().getName());
				cnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			FeatureExtractionException e1 = new FeatureExtractionException();
			e1.setFile(file);
			e1.setExcepion(e);
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
		attribs.add(FEATURE_KEY);
		attribs.add(FEATURE_LYRICIST);
		attribs.add(FEATURE_MOOD);
		attribs.add(FEATURE_ISCOMPILATION);
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
