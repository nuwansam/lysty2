package org.lysty.strategies.metatagmatch;

import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.lysty.dao.Song;
import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.AbstractVoteMatchStrategy;
import org.lysty.util.Utils;

@PluginImplementation
public class MetaTagMatchStrategy extends AbstractVoteMatchStrategy {

	private static final int VOTES_ON_MATCH = 10;
	public static final String FEATURE_TO_MATCH = "FEATURE_TO_MATCH";
	private static final String FEATURE_ARTIST = "artist";
	private String feature;

	@Override
	protected int getVotes(Song song, Song candidate) {
		if (feature == null)
			feature = FEATURE_ARTIST;
		String sArtist = song.getAttribute(feature);
		String cArtist = candidate.getAttribute(feature);

		if (Utils.stringNotNullOrEmpty(sArtist)
				&& Utils.stringNotNullOrEmpty(cArtist)) {
			sArtist = sArtist.toLowerCase().trim();
			cArtist = cArtist.toLowerCase().trim();
			if (sArtist.contains(cArtist) || cArtist.contains(sArtist)) {
				return VOTES_ON_MATCH;
			}
			return 0;
		}
		return 0;
	}

	@Override
	public String getStrategyDisplayName() {
		return "Meta tag Match";
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "MetaTagMatch";
	}

	@Override
	public AbstractStrategySettingsPanel getStrategySettingsFrame() {
		return new MetaTagMatchStrategySettingsFrame();
	}

	@Override
	protected void readAttributes(Map<String, String> attributes) {
		feature = attributes.get(FEATURE_TO_MATCH);
	}
}
