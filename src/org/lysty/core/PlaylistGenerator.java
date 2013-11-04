package org.lysty.core;

import java.util.List;

import net.xeoh.plugins.base.Plugin;

import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.StrategyConfiguration;

/**
 * Interface to be implemented by playlist generator stratergies
 * 
 * @author NuwanSam
 * 
 */
public interface PlaylistGenerator extends Plugin {

	/**
	 * 
	 * @param profile
	 *            song selection profile
	 * @param attributes
	 *            additional settings that are required by the strategy to
	 *            create the playlist (strategy specific settings)
	 * 
	 * @return completed playlist
	 */
	public List<Song> getPlaylist(SongSelectionProfile profile,
			StrategyConfiguration config, boolean isCircular,
			boolean mustIncludeSeeds, List<Song> blacklist); // The attribute
																// map will
																// contain
	//

	/**
	 * Get the display name of the strategy
	 * 
	 * @return
	 */
	public String getStrategyDisplayName();

	public String toString();

	public AbstractStrategySettingsPanel getStrategySettingsFrame();
}
