package org.lysty.core;

import org.lysty.dao.Song;
import org.lysty.players.AbstractPlayer;
import org.lysty.players.PlayEvent;
import org.lysty.players.PlaybackListener;
import org.lysty.players.PlayerManager;
import org.lysty.ui.exception.SongPlayException;

public class SongPlayer {

	public static SongPlayer self = null;

	public static SongPlayer getInstance() {
		if (self == null) {
			self = new SongPlayer();
		}
		return self;
	}

	private AbstractPlayer player;

	private SongPlayer() {

	}

	public void play(final Song song, int playFrom,
			final PlaybackListener playbackListener) {
		try {
			stop(); // stop the currently playing song
		} catch (Exception e) {
			e.printStackTrace();
		}
		player = PlayerManager.getInstance().getPlayer(song.getFileType());
		player.setPlaybackListener(playbackListener);
		Thread thread = new SongPlayThread(song, playFrom,
				new ExceptionListener() {

					@Override
					public void notifyException(Exception e) {
						playbackListener.getNotification(new PlayEvent(
								PlayEvent.EventType.PLAY_EXCEPTION));
					}
				});
		thread.start();
	}

	public void pause() {
		if (player != null)
			player.pause();
		player = null;
	}

	public void stop() {
		if (player != null)
			player.stop();
		player = null;
	}

	class SongPlayThread extends Thread {
		Song song;
		private ExceptionListener exceptionListener;
		private int playFrom;

		public SongPlayThread(Song song, int playFrom,
				ExceptionListener exceptionListener) {
			this.song = song;
			this.playFrom = playFrom;
			this.exceptionListener = exceptionListener;
		}

		public void run() {
			try {
				if (player != null)
					player.play(song, playFrom);
			} catch (SongPlayException e) {
				// TODO Auto-generated catch block
				exceptionListener.notifyException(e);
			}
		}
	}

	interface ExceptionListener {
		public void notifyException(Exception e);
	}

}
