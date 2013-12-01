package org.lysty.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.lysty.core.AppSettingsManager;
import org.lysty.core.PlaylistGenerator;
import org.lysty.core.StrategyFactory;
import org.lysty.dao.Song;
import org.lysty.strategies.StrategyConfiguration;
import org.lysty.util.Utils;

public class PlayerPanel extends JPanel implements StrategySettingsListener {

	private static final String FEATURE_DURATION = "duration";
	private static final int DEFAULT_FRAMES_PER_SECS = 38;
	private JProgressBar progress;
	private JToggleButton btnStartPause;
	private JButton btnStop;
	private JButton btnNext;
	private JButton btnPrev;
	private PlayPanelListener listener;
	private JToggleButton btnInfin;
	private JButton btnSleep;
	private JToggleButton btnRandom;

	private static final int MINS_TO_MILIS = 60000;

	public enum PlayState {
		PLAYING, PAUSED, STOPPED
	}

	private PlayState playState;
	private Timer timer;
	private int pausedOnFrame;
	private JComboBox cmbStrategy;
	private JButton btnFillSettings;
	private PlaylistGenerator currentStrategy;
	private StrategyConfiguration currentStrategySettings;
	private JSlider volumeSlider;
	private JLabel lblTime;
	private Logger logger = Logger.getLogger(PlayerPanel.class);

	/**
	 * @return the currentStrategy
	 */
	public PlaylistGenerator getCurrentStrategy() {
		return currentStrategy;
	}

	/**
	 * @param currentStrategy
	 *            the currentStrategy to set
	 */
	public void setCurrentStrategy(PlaylistGenerator currentStrategy) {
		this.currentStrategy = currentStrategy;
	}

	/**
	 * @return the currentStrategySettings
	 */
	public StrategyConfiguration getCurrentStrategySettings() {
		return currentStrategySettings;
	}

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
			btnStartPause.setIcon(Utils.getIcon(ResourceConstants.PAUSE_ICON));
			btnStartPause.setSelected(true);
		} else if (state == PlayState.PAUSED) {
			btnStartPause.setIcon(Utils.getIcon(ResourceConstants.PLAY_ICON));
			btnStartPause.setSelected(false);
			if (timer != null)
				timer.stop();
		} else if (state == PlayState.STOPPED) {
			btnStartPause.setIcon(Utils.getIcon(ResourceConstants.PLAY_ICON));
			btnStartPause.setSelected(false);
			if (timer != null)
				timer.stop();
			setCurrentSongProgress(0);
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

	public void loadLatestSettings() {
		String strategyClass = AppSettingsManager
				.getProperty(AppSettingsManager.LS_FILL_STRATEGY);
		if (Utils.stringNotNullOrEmpty(strategyClass)) {
			PlaylistGenerator strategy = StrategyFactory
					.getStrategyByClassName(strategyClass);
			if (strategy != null)
				cmbStrategy.setSelectedItem(strategy);
		}
		btnInfin.setSelected(AppSettingsManager
				.getPropertyAsBoolean(AppSettingsManager.LS_INFINI_PLAY));
		listener.setInfinyPlay(btnInfin.isSelected());
		volumeSlider.setValue(Integer.parseInt(AppSettingsManager.getProperty(
				AppSettingsManager.LS_VOLUME_LEVEL, "90")));

	}

	private void layoutControls() {
		setLayout(new MigLayout("", "[grow]", "[][grow]"));

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));

		add(panel, "cell 0 0,grow");
		panel.setLayout(new MigLayout("", "[][][][][][][]", "[][]"));

		panel.add(progress, "flowx,spanx 6,growx");
		panel.add(lblTime, "flowx,align right,span");

		panel.add(btnStartPause, "flowx");

		panel.add(btnStop, "flowx");

		panel.add(btnPrev, "flowx");

		panel.add(btnNext, "flowx");

		panel.add(volumeSlider, "flowx,push,span 2,alignx right");
		panel.add(btnRandom, "flowx,push,align right,split 2,gapleft 5");
		panel.add(btnSleep, "flowx,push,alignx right");

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		add(panel_1, "cell 0 1,grow");
		panel_1.setLayout(new MigLayout("", "[][grow][]", "[]"));

		panel_1.add(btnInfin, "cell 0 0,aligny top");

		panel_1.add(cmbStrategy, "cell 1 0,growx");

		panel_1.add(btnFillSettings, "cell 2 0");
	}

	protected void showSettingsFrame() {
		PlaylistGenerator strategy = (PlaylistGenerator) cmbStrategy
				.getSelectedItem();
		StrategySettingsWindow.getInstance().createUi(strategy, this);
		StrategySettingsWindow.getInstance().setListener(this);
	}

	public void setCurrentSong(Song song, int playFrom) {
		try {
			int duration = 0;
			if (song.getAttribute(FEATURE_DURATION) != null) {
				duration = Integer
						.parseInt(song.getAttribute(FEATURE_DURATION));
			} else {
				try {
					AudioFile audioFile = AudioFileIO.read(song.getFile());
					duration = audioFile.getAudioHeader().getTrackLength();
				} catch (Exception e) {
					logger.error("Couldn't extract audio duraion for song: "
							+ song.getFile().getName(), e);
					duration = 0;
				}
			}
			final int fDuration = duration;
			if (playFrom == 0) {
				setCurrentSongProgress(0);
				progress.setMaximum(duration);
				progress.setString("");
				progress.setStringPainted(true);
			}
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					// ...Update the progress bar...
					setCurrentSongProgress(progress.getValue() + 1);
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
		progress.setPreferredSize(new Dimension(300, 10));
		progress.setMaximumSize(new Dimension(300, 10));
		progress.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int tW = progress.getWidth();
				int cW = e.getX();
				int newProgress = progress.getMaximum() * cW / tW;
				listener.play((int) newProgress * DEFAULT_FRAMES_PER_SECS);
				setCurrentSongProgress(newProgress);

			}
		});
		progress.setMaximum(0);
		lblTime = new JLabel("-.-/-.-");

		btnStartPause = new JToggleButton();
		btnStartPause.setIcon(Utils.getIcon(ResourceConstants.PLAY_ICON));
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
		btnStop = new JButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setState(PlayState.STOPPED);
				pausedOnFrame = 0;
				listener.stop();
			}
		});
		btnStop.setIcon(Utils.getIcon(ResourceConstants.STOP_ICON));
		btnNext = new JButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.stop();
				setState(PlayState.STOPPED);
				listener.next();
			}
		});
		btnNext.setIcon(Utils.getIcon(ResourceConstants.NEXT_ICON));
		btnPrev = new JButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.stop();
				setState(PlayState.STOPPED);
				listener.prev();
			}
		});
		btnPrev.setIcon(Utils.getIcon(ResourceConstants.PREV_ICON));

		btnInfin = new JToggleButton(
				Utils.getIcon(ResourceConstants.INFINI_ICON));
		btnInfin.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				listener.setInfinyPlay(btnInfin.isSelected());
			}
		});
		final JPopupMenu popup = new SleepMenu(listener);
		btnSleep = new JButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {

				popup.show(btnSleep, 0, 0);
			}
		});

		btnSleep.setIcon(Utils.getIcon(ResourceConstants.SLEEP_ICON));

		btnRandom = new JToggleButton(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.setRandomize(btnRandom.isSelected());
			}
		});
		btnRandom.setIcon(Utils.getIcon(ResourceConstants.SHUFFLE_ICON));
		btnRandom.setSelected(false);

		btnFillSettings = new JButton(new AbstractAction("") {

			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsFrame();
			}
		});
		btnFillSettings.setIcon(Utils.getIcon(ResourceConstants.SETTINGS_ICON));
		cmbStrategy = new JComboBox();
		List<PlaylistGenerator> list = StrategyFactory.getAllStrategies();
		cmbStrategy.setModel(new DefaultComboBoxModel(list
				.toArray(new PlaylistGenerator[list.size()])));
		cmbStrategy.setRenderer(new ListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel lbl = new JLabel(((PlaylistGenerator) value)
						.getStrategyDisplayName());
				return lbl;
			}
		});
		cmbStrategy.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					PlaylistGenerator item = (PlaylistGenerator) event
							.getItem();
					onStrategySelectionChange(item);
				}
			}
		});

		volumeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
		volumeSlider.setToolTipText("Volume control");
		volumeSlider.setPreferredSize(new Dimension(60, 10));
		// volumeSlider.setMaximumSize(new Dimension(60, 20));
		// volumeSlider.setMinimumSize(new Dimension(60, 5));

		volumeSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				VolumeControl.setVolume((float) volumeSlider.getValue() / 100);
			}
		});
		cmbStrategy.setSelectedIndex(0);
		onStrategySelectionChange((PlaylistGenerator) cmbStrategy.getItemAt(0));

	}

	protected void onStrategySelectionChange(PlaylistGenerator newStrategy) {
		currentStrategy = newStrategy;
		currentStrategySettings = StrategyFactory
				.getDefaultOrLastSettings(newStrategy);

	}

	public void setCurrentSongProgress(int val) {
		progress.setValue(val);
		lblTime.setText(getTimeFormattedString(val) + "/"
				+ getTimeFormattedString(progress.getMaximum()));
	}

	private String getTimeFormattedString(int val) {
		int mins = val / 60;
		int secs = val % 60;
		return mins + "." + ((secs < 10) ? "0" + secs : secs);
	}

	public void setPausedOnFrame(int frame) {
		this.pausedOnFrame = frame;
	}

	public boolean getIsInfiniPlay() {
		return btnInfin.isSelected();
	}

	@Override
	public void setCurrentProfileSettings(StrategyConfiguration strategySettngs) {
		currentStrategySettings = strategySettngs;
		StrategyFactory.updateLastSettings(currentStrategy,
				currentStrategySettings);
	}

	public void setInfiniPlay(boolean selected) {
		btnInfin.setSelected(selected);
	}

	public void setStrategy(PlaylistGenerator strategy) {
		currentStrategy = strategy;
		cmbStrategy.setSelectedItem(currentStrategy);
	}

	public void updateSettings() {
		AppSettingsManager.setProperty(AppSettingsManager.LS_FILL_STRATEGY,
				((PlaylistGenerator) cmbStrategy.getSelectedItem()).getClass()
						.getName());
		AppSettingsManager.setProperty(AppSettingsManager.LS_INFINI_PLAY,
				btnInfin.isSelected() ? "true" : "false");
		AppSettingsManager.setProperty(AppSettingsManager.LS_VOLUME_LEVEL,
				volumeSlider.getValue() + "");
	}

	public void setToolTips() {
		btnStartPause.setToolTipText("Start/Pause");
		btnStop.setToolTipText("Stop");
		btnPrev.setToolTipText("Previous Song");
		btnNext.setToolTipText("Next Song");
		btnRandom.setToolTipText("Randomize");
		btnSleep.setToolTipText("Sleep");
		cmbStrategy
				.setToolTipText("Fill strategy to use to generate the next songs for the playlist");
		btnFillSettings.setToolTipText("Settings for the fill strategy");
		btnInfin.setToolTipText("InfiniPlay Mode");
	}

}
