package org.lysty.strategies.cummetamatch;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.lysty.dao.Song;
import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.AbstractVoteMatchPriorityStrategy;
import org.lysty.strategies.AbstractVoteMatchStrategy;
import org.lysty.util.FileUtils;
import org.lysty.util.Utils;

@PluginImplementation
public class CumulativeMetaMatchStrategy extends
		AbstractVoteMatchPriorityStrategy {

	public static final String ALBUM_VOTE_WEIGHT = "album_weight";
	public static final String RELEASE_DATE_VOTE_WEIGHT = "release_date_weight";
	public static final String ARTIST_VOTE_WEIGHT = "artist_weight";
	public static final String GENRE_VOTE_WEIGHT = "genre_weight";
	public static final String COMPOSER_VOTE_WEIGHT = "composer_weight";
	public static final String LANGUAGE_VOTE_WEIGHT = "language_weight";
	public static final String MOOD_VOTE_WEIGHT = "mood_weight";
	public static final String FOLDER_VOTE_WEIGHT = "folder_weight";
	public static final String COMMON_FOLDER_HEIGHT = "common_folder_height";

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
	private static final String EXACT_MATCH = "exact_match";
	private static final String CONTAINS_MATCH = "contains_match";

	private int albumW;
	private int releaseDateW;
	private int artistW;
	private int genreW;
	private int composerW;
	private int langW;
	private int moodW;
	private int folderW;
	private int commonFolderHeight;

	@Override
	public String getStrategyDisplayName() {
		// TODO Auto-generated method stub
		return "Cumulative Meta Match";
	}

	@Override
	public AbstractStrategySettingsPanel getStrategySettingsFrame() {
		return new CumulativeMetaMatchStrategySettings();
	}

	@Override
	protected void readAttributes(Map<String, String> attributes) {
		albumW = Integer.parseInt(attributes.get(ALBUM_VOTE_WEIGHT));
		releaseDateW = Integer.parseInt(attributes
				.get(RELEASE_DATE_VOTE_WEIGHT));
		artistW = Integer.parseInt(attributes.get(ARTIST_VOTE_WEIGHT));
		genreW = Integer.parseInt(attributes.get(GENRE_VOTE_WEIGHT));
		composerW = Integer.parseInt(attributes.get(COMPOSER_VOTE_WEIGHT));
		langW = Integer.parseInt(attributes.get(LANGUAGE_VOTE_WEIGHT));
		moodW = Integer.parseInt(attributes.get(MOOD_VOTE_WEIGHT));
		folderW = Integer.parseInt(attributes.get(FOLDER_VOTE_WEIGHT));
		commonFolderHeight = Integer.parseInt(attributes
				.get(COMMON_FOLDER_HEIGHT));
	}

	private int getVotesForAttribute(Song song, Song candidate, String feature,
			int weight, String compareMethod) {
		String sAttr = song.getAttribute(feature);
		String cAttr = candidate.getAttribute(feature);
		if (sAttr != null)
			sAttr = sAttr.trim();
		if (cAttr != null)
			cAttr = cAttr.trim();

		if (Utils.stringNotNullOrEmpty(sAttr)
				&& Utils.stringNotNullOrEmpty(cAttr)) {
			if (EXACT_MATCH.equals(compareMethod)) {
				if (sAttr.equalsIgnoreCase(cAttr)) {
					return weight;
				}
			}
			if (CONTAINS_MATCH.equals(compareMethod)) {
				if (sAttr.contains(cAttr) || cAttr.contains(sAttr)) {
					return weight;
				}
			}
		}
		return 0;
	}

	@Override
	protected int getVotes(Song song, Song candidate) {
		int votes = 0;
		int folderDist = FileUtils.getDistanceToCommonFolder(song.getFile(),
				candidate.getFile());

		votes += getVotesForAttribute(song, candidate, FEATURE_ALBUM, albumW,
				EXACT_MATCH);
		votes += getVotesForAttribute(song, candidate, FEATURE_RELEASEDATE,
				releaseDateW, EXACT_MATCH);
		votes += getVotesForAttribute(song, candidate, FEATURE_ARTIST, artistW,
				CONTAINS_MATCH);

		// for genre, do not consider matches if either is other
		String songGenreStr;
		songGenreStr = song.getAttribute(FEATURE_GENRE);
		if (!"other".equalsIgnoreCase(songGenreStr)) {
			votes += getVotesForAttribute(song, candidate, FEATURE_GENRE,
					genreW, CONTAINS_MATCH);
		}

		votes += getVotesForAttribute(song, candidate, FEATURE_COMPOSER,
				composerW, CONTAINS_MATCH);
		votes += getVotesForAttribute(song, candidate, FEATURE_LANGUAGE, langW,
				EXACT_MATCH);
		votes += getVotesForAttribute(song, candidate, FEATURE_MOOD, moodW,
				EXACT_MATCH);

		if (folderDist > commonFolderHeight) {
			votes += folderW / folderDist;
		}
		return votes;
	}

	@Override
	public String toString() {
		return "CumulativeMetaMatch";
	}
}
