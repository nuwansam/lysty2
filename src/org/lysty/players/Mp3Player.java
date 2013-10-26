package org.lysty.players;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import javazoom.jl.player.PlayerApplet;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import org.apache.log4j.Logger;
import org.lysty.dao.Song;
import org.lysty.ui.exception.SongPlayException;

public class Mp3Player extends AbstractPlayer {

	private static Logger logger = Logger.getLogger(Mp3Player.class);
	private AdvancedPlayer player;

	@Override
	public List<String> getSupportedFormats() {
		// TODO Auto-generated method stub
		ArrayList<String> list = new ArrayList<String>();
		list.add("mp3");
		return list;
	}

	@Override
	public void play(Song song) throws SongPlayException {
		try {
			player = new AdvancedPlayer(new BufferedInputStream(
					new FileInputStream(song.getFile())));
			PlaybackListener listener = new PlaybackListener() {
				@Override
				public void playbackStarted(PlaybackEvent arg0) {
					playbackListener.getNotification(new PlayEvent(
							PlayEvent.EventType.PLAY_STARTED));
				}

				@Override
				public void playbackFinished(PlaybackEvent arg0) {
					playbackListener.getNotification(new PlayEvent(
							PlayEvent.EventType.PLAY_FINISHED));
				}
			};
			player.setPlayBackListener(listener);
			player.play();
		} catch (Exception e) {
			logger.error("Song file not found", e);
			throw new SongPlayException();
		}

	}

	@Override
	public void pause() {
		player.stop();
	}

	@Override
	public void stop() {
		player.stop();
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
