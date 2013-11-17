package org.lysty.players;

import java.util.List;

import org.lysty.dao.Song;
import org.lysty.ui.exception.SongPlayException;

public abstract class AbstractPlayer {

	public PlaybackListener playbackListener;

	public abstract List<String> getSupportedFormats();

	public abstract void play(Song song, int playFrom) throws SongPlayException;

	public abstract void changePlayPosition(int newPosition)
			throws SongPlayException;

	public abstract void pause();

	public abstract void stop();

	public void setPlaybackListener(PlaybackListener listener) {
		playbackListener = listener;
	}

}
