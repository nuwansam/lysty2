package org.lysty.players;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.apache.log4j.Logger;
import org.lysty.dao.Song;
import org.lysty.ui.exception.SongPlayException;

public class Mp3Player extends AbstractPlayer {

	private static Logger logger = Logger.getLogger(Mp3Player.class);
	private AdvancedPlayer player;
	private boolean forceStopped;
	private boolean forcePaused;

	@Override
	public List<String> getSupportedFormats() {
		// TODO Auto-generated method stub
		ArrayList<String> list = new ArrayList<String>();
		list.add("mp3");
		return list;
	}

	@Override
	public void play(final Song song, final int playFrom)
			throws SongPlayException {
		try {
			forceStopped = false;
			forcePaused = false;
			player = new AdvancedPlayer(new BufferedInputStream(
					new FileInputStream(song.getFile())));
			PlaybackListener listener = new PlaybackListener() {
				@Override
				public void playbackStarted(PlaybackEvent arg0) {
					playbackListener.getNotification(new PlayEvent(
							PlayEvent.EventType.PLAY_STARTED));
				}

				@Override
				public void playbackFinished(PlaybackEvent event) {
					PlayEvent playEvent = new PlayEvent(
							forceStopped ? PlayEvent.EventType.SONG_STOPPED
									: forcePaused ? PlayEvent.EventType.SONG_PAUSED
											: PlayEvent.EventType.SONG_ENDED);
					playEvent.setFrame(playFrom
							+ (44 * event.getFrame() / 1000));

					playbackListener.getNotification(playEvent);
				}
			};
			player.setPlayBackListener(listener);
			player.play(playFrom, Integer.MAX_VALUE);
		} catch (Exception e) {
			logger.error("Song file not found", e);
			throw new SongPlayException();
		}

	}

	@Override
	public void pause() {
		forceStopped = false;
		forcePaused = true;
		if (player != null)
			player.stop();
		player = null;
	}

	@Override
	public void stop() {
		try {
			forceStopped = true;
			forcePaused = false;
			if (player != null)
				player.stop();
			player = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setVolume(int volume) {

	}

	@Override
	public void changePlayPosition(int newPosition) throws SongPlayException {
		try {
			player.play(newPosition);
		} catch (JavaLayerException e) {
			logger.error("Error in changing play position", e);
			throw new SongPlayException();
		}
	}

}
