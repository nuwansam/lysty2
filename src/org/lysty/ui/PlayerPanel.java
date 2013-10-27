package org.lysty.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.lysty.dao.Song;
import org.lysty.extractors.MetaTagExtractor;

public class PlayerPanel extends JPanel {

	private JProgressBar progress;
	private JToggleButton btnStartPause;
	private JButton btnStop;
	private JButton btnNext;
	private JButton btnPrev;
	private PlayPanelListener listener;
	private JCheckBox chkInfin;
	private JButton btnSleep;
	private JToggleButton btnRandom;

	public enum PlayState {
		PLAYING, PAUSED, STOPPED
	}

	private PlayState playState;
	private Timer timer;
	private int pausedOnFrame;

	public PlayState getState() {
		return playState;
	}

	public void setState(PlayState state) {
		if (playState == state) {
			// no state changed
			return;
		}
		playState = state;
		if (state == PlayState.PLAYING) {
			btnStartPause.setText("Pause");
			btnStartPause.setSelected(true);
		} else if (state == PlayState.PAUSED) {
			btnStartPause.setText("Start");
			btnStartPause.setSelected(false);
			if (timer != null)
				timer.stop();
		} else if (state == PlayState.STOPPED) {
			btnStartPause.setText("Start");
			btnStartPause.setSelected(false);
			if (timer != null)
				timer.stop();
			progress.setValue(0);
		}
	}

	public int getCurrentProgress() {
		return progress.getValue();
	}

	public PlayerPanel(PlayPanelListener listener) {
		this.listener = listener;
		createUI();
		layoutControls();
		setState(PlayState.STOPPED);
	}

	private void layoutControls() {
		MigLayout layout = new MigLayout();
		this.setLayout(layout);
		this.add(progress, "span");
		this.add(btnStop);
		this.add(btnStartPause);
		this.add(btnPrev);
		this.add(btnNext, "wrap");
		this.add(chkInfin);
		this.add(btnRandom);
		this.add(btnSleep, "align right");
	}

	public void setCurrentSong(Song song) {
		try {
			int duration = 0;
			if (song.getAttribute(MetaTagExtractor.FEATURE_DURATION) != null) {
				duration = Integer.parseInt(song
						.getAttribute(MetaTagExtractor.FEATURE_DURATION));
			} else {
				AudioFile audioFile = AudioFileIO.read(song.getFile());
				duration = audioFile.getAudioHeader().getTrackLength();
			}
			final int fDuration = duration;
			progress.setMaximum(duration);
			progress.setString("");
			progress.setStringPainted(true);
			timer = new Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					// ...Update the progress bar...
					progress.setValue(progress.getValue() + 1);
					if (progress.getValue() >= fDuration) {
						timer.stop();
					}
				}
			});
			timer.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createUI() {
		progress = new JProgressBar();
		progress.setPreferredSize(new Dimension(400, 5));
		progress.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int tW = progress.getWidth();
				int cW = e.getX();
				int newProgress = progress.getMaximum() * cW / tW;
				listener.play(newProgress);
			}
		});
		btnStartPause = new JToggleButton("Start");
		btnStartPause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (btnStartPause.isSelected()) {
					// pause
					setState(PlayState.PLAYING);
					listener.play(pausedOnFrame);
				} else {
					// start
					setState(PlayState.PAUSED);
					listener.pause();
				}
			}
		});
		btnStop = new JButton(new AbstractAction("Stop") {

			@Override
			public void actionPerformed(ActionEvent e) {
				setState(PlayState.STOPPED);
				pausedOnFrame = 0;
				listener.stop();
			}
		});
		btnNext = new JButton(new AbstractAction("Next") {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.stop();
				setState(PlayerPanel.PlayState.PLAYING);
				listener.next();
			}
		});
		btnPrev = new JButton(new AbstractAction("Prev") {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.stop();
				setState(PlayerPanel.PlayState.PLAYING);
				listener.prev();
			}
		});

		chkInfin = new JCheckBox("Infini Play");
		chkInfin.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				listener.setInfinyPlay(chkInfin.isSelected());
			}
		});
		btnSleep = new JButton(new AbstractAction("Sleep") {

			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem mnuHour = new JMenuItem(new AbstractAction("1 Hour") {

					@Override
					public void actionPerformed(ActionEvent e) {
						listener.setTimer(60);
					}
				});
				popup.add(mnuHour);
				popup.show(btnSleep, 0, 0);
			}
		});
		btnRandom = new JToggleButton(new AbstractAction("Non Random") {

			@Override
			public void actionPerformed(ActionEvent e) {
				btnRandom.setText(btnRandom.isSelected() ? "Random"
						: "Non Random");
				listener.setRandomize(btnRandom.isSelected());
			}
		});
		btnRandom.setSelected(false);

	}

	public void setCurrentProgress(int val) {
		progress.setValue(val);
	}

	public void setPausedOnFrame(int frame) {
		this.pausedOnFrame = frame;
	}
}
