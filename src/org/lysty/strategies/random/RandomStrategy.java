package org.lysty.strategies.random;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.lysty.dao.Song;
import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.AbstractVoteMatchStrategy;
import org.lysty.util.Utils;

@PluginImplementation
public class RandomStrategy extends AbstractVoteMatchStrategy {

	public static final String COMMON_FOLDER_HEIGHT = "COMMON_FOLDER_HEIGHT";
	public static final Integer DEFAULT_COMMON_FOLDER_HEIGHT = 2;
	private String useFolderDist;
	Integer commonFolderHeight;

	@Override
	public String getStrategyDisplayName() {
		return "Random";
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Random";
	}

	@Override
	public AbstractStrategySettingsPanel getStrategySettingsFrame() {
		return new RandomStrategySettingsPanel();
	}

	@Override
	protected int getVotes(Song song, Song candidate) {
		File songFile = song.getFile();
		File candidateFile = candidate.getFile();
		File songParentFile = songFile;
		int votes = 0;
		for (int i = 1; i < commonFolderHeight; i++) {
			songParentFile = songParentFile.getParentFile();
			if (songParentFile == null)
				break;
		}
		String songParentPath = songParentFile == null ? "" : songParentFile
				.getAbsolutePath().toLowerCase();
		if (candidateFile.getAbsolutePath().toLowerCase()
				.startsWith(songParentPath)) {
			// viable candidate;
			votes += 10;
		}
		return votes;
	}

	@Override
	protected void readAttributes(Map<String, String> attributes) {
		String str = attributes.get(COMMON_FOLDER_HEIGHT);
		commonFolderHeight = DEFAULT_COMMON_FOLDER_HEIGHT;
		if (Utils.stringNotNullOrEmpty(str)) {
			commonFolderHeight = Integer.parseInt(str);
		}
		useFolderDist = attributes.get("USE_FOLDER_DIST");
	}

}
