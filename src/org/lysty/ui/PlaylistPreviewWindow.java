package org.lysty.ui;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.DropMode;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.lysty.core.AppSettingsManager;
import org.lysty.core.PlaylistGenerator;
import org.lysty.core.SongPlayer;
import org.lysty.core.StrategyFactory;
import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.db.DBHandler;
import org.lysty.players.PlayEvent;
import org.lysty.players.PlaybackListener;
import org.lysty.strategies.StrategyConfiguration;
import org.lysty.ui.PlayerPanel.PlayState;
import org.lysty.ui.exception.SongNotIndexedException;
import org.lysty.ui.model.PlaylistModel;
import org.lysty.util.FileUtils;
import org.lysty.util.Utils;

public class PlaylistPreviewWindow extends LFrame implements PlayPanelListener {

	private static final int DEFAULT_GENLIST_SIZE = 5;
	// private static final int genListSize = 5;
	private static final int DEFAULT_INFINIPLAY_LAST_N_TO_CHECK = 8;
	private List<Song> list;
	private PlaylistModel model;
	private int currentSongIndex;
	private Set<Song> played;
	private JScrollPane scrollPane;
	private JTable table;
	private PlayerPanel playerPanel;
	private boolean isRandomized;
	private SongSelectionProfile selProfile;
	private List<Song> manuallyAdded;
	private List<Song> manuallySkipped;
	private PlaybackListener playbackListener;
	private Timer timer;
	static Logger logger = Logger.getLogger(PlaylistPreviewWindow.class);

	private static PlaylistPreviewWindow self = null;

	public static PlaylistPreviewWindow getInstance() {
		if (self == null) {
			self = new PlaylistPreviewWindow();
		}
		return self;
	}

	private PlaylistPreviewWindow() {
		super("Lysty Media Player");
		createUI();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				updateSettings();
				AppSettingsManager.writeAppSettings();
			}
		});
		playbackListener = new PlaybackListener() {

			@Override
			public void getNotification(PlayEvent event) {
				if (event.getEventType() == PlayEvent.EventType.SONG_ENDED) {
					// previous song has ended;
					playerPanel.setState(PlayState.STOPPED);
					playNextSong();
				} else if (event.getEventType() == PlayEvent.EventType.PLAY_EXCEPTION) {
					JOptionPane.showMessageDialog(PlaylistPreviewWindow.this,
							"Error playing song: ");
					playerPanel.setState(PlayerPanel.PlayState.STOPPED);
				} else if (event.getEventType() == PlayEvent.EventType.SONG_PAUSED) {
					playerPanel.setPausedOnFrame(event.getFrame());
				} else if (event.getEventType() == PlayEvent.EventType.SONG_STOPPED) {
					playerPanel.setPausedOnFrame(0);
				}
			}
		};
		init(new ArrayList<Song>(), false, null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void loadSettings() {
		String xStr = AppSettingsManager.getProperty(AppSettingsManager.LS_X);
		String yStr = AppSettingsManager.getProperty(AppSettingsManager.LS_Y);
		if (Utils.isNumber(xStr) && Utils.isNumber(yStr)) {
			setLocation(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
	}

	protected void updateSettings() {
		AppSettingsManager.setProperty(AppSettingsManager.LS_X, getX() + "");
		AppSettingsManager.setProperty(AppSettingsManager.LS_Y, getY() + "");
		playerPanel.updateSettings();
	}

	private void createUI() {
		table = new JTable();
		model = new PlaylistModel();

		table.setModel(model);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table));
		TableDragDropListener myDragDropListener = new TableDragDropListener(
				table);
		new DropTarget(table, myDragDropListener);

		scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		TableColumnModel colModel = table.getColumnModel();
		colModel.getColumn(0).setPreferredWidth(Integer.MAX_VALUE);
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					currentSongIndex = row;
					stop();
					playerPanel.setState(PlayerPanel.PlayState.PLAYING);
					playerPanel.setCurrentSongProgress(0);
					play(0);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				int r = table.rowAtPoint(e.getPoint());
				if (r >= 0 && r < table.getRowCount()) {
					table.setRowSelectionInterval(r, r);
				} else {
					table.clearSelection();
				}

				final int rowindex = table.getSelectedRow();
				if (rowindex < 0)
					return;
				if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
					JPopupMenu tablePopup = new JPopupMenu();
					JMenuItem mnuRem = new JMenuItem(new AbstractAction(
							"Remove") {

						@Override
						public void actionPerformed(ActionEvent e) {
							if(rowindex==currentSongIndex){
								stop();
							}
							model.removeRow(rowindex);
						}
					});
					tablePopup.add(mnuRem);
					tablePopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		table.setTableHeader(null);

		createMenu();
		loadSettings();
		layoutControls();
		setToolTips();
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private void setToolTips() {
		playerPanel.setToolTips();
	}

	private void createMenu() {
		JMenuBar menu = new JMenuBar();
		JMenu mnuFile = new JMenu("File");
		JMenuItem mnuFileSave = new JMenuItem(new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.addChoosableFileFilter(new FileNameExtensionFilter(
						".m3u", "m3u"));
				int c = chooser.showSaveDialog(PlaylistPreviewWindow.this);
				if (c != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File file = chooser.getSelectedFile();
				try {
					if (!file.getName().contains(".")) { // no ext provided.
															// default to .m3u
						file = new File(file.getAbsolutePath() + ".m3u");
					}
					boolean success = FileUtils.savePlaylist(list, file);
				} catch (IOException e1) {
					logger.error(
							"Error saving playlist to: "
									+ file.getAbsolutePath(), e1);
				}

			}
		});

		JMenuItem mnuAddSong = new JMenuItem(new AbstractAction("Add Songs") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				int c = chooser.showOpenDialog(PlaylistPreviewWindow.this);
				if (c == JFileChooser.APPROVE_OPTION) {
					File[] files = chooser.getSelectedFiles();
					Song song;
					for (File file : files) {
						song = DBHandler.getInstance().getSong(file);
						if (song == null) {
							song = new Song(file);
						}
						addSongNext(song);
					}
				}
			}
		});

		JMenuItem mnuClear = new JMenuItem(
				new AbstractAction("Clear Playlist") {

					@Override
					public void actionPerformed(ActionEvent e) {
						stop();
						init(new ArrayList<Song>(), false, null);
						model.fireTableDataChanged();
					}
				});

		JMenuItem mnuLoadPlaylist = new JMenuItem(new AbstractAction(
				"Load Playlist") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
				chooser.addChoosableFileFilter(new FileNameExtensionFilter(
						".m3u", "m3u"));
				int c = chooser.showOpenDialog(PlaylistPreviewWindow.this);
				if (c != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File file = chooser.getSelectedFile();
				loadFromPlayList(file);
			}
		});

		JMenuItem mnuExit = new JMenuItem(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		JMenu mnuExport = new JMenu("Export");
		JMenuItem mnuExportCopy = new JMenuItem(new AbstractAction(
				"Copy Files...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				copyFiles();
			}
		});
		mnuExport.add(mnuExportCopy);

		JMenu mnuTools = new JMenu("Tools");

		JMenuItem mnuPPEdit = new JMenuItem(new AbstractAction(
				"Partial Playlist Editor") {

			@Override
			public void actionPerformed(ActionEvent e) {
				PlaylistProfileWindow instance = PlaylistProfileWindow
						.getInstance();
				instance.setVisible(true);
				instance.setLocation(PlaylistPreviewWindow.this.getX()
						+ PlaylistPreviewWindow.this.getWidth(),
						PlaylistPreviewWindow.this.getY());
			}
		});

		JMenuItem mnuEditMetaData = new JMenuItem(new AbstractAction(
				"Edit MetaData") {

			@Override
			public void actionPerformed(ActionEvent e) {
				MetaDataEditor.getInstance().createUI();
				MetaDataEditor.getInstance().setVisible(true);
			}
		});

		JMenuItem mnuToolsIndex = new JMenuItem(new AbstractAction("Index...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				Commands.showIndexDialog(PlaylistPreviewWindow.this);
			}
		});

		JMenuItem mnuToolsSettings = new JMenuItem(new AbstractAction(
				"Settings") {

			@Override
			public void actionPerformed(ActionEvent e) {
				AppSettingsWindow instance = AppSettingsWindow.getInstance();
				instance.init(PlaylistPreviewWindow.this);
			}
		});

		mnuTools.add(mnuPPEdit);
		mnuTools.addSeparator();
		mnuTools.add(mnuEditMetaData);
		mnuTools.add(mnuToolsIndex);
		mnuTools.addSeparator();
		mnuTools.add(mnuToolsSettings);

		mnuFile.add(mnuClear);
		mnuFile.addSeparator();
		mnuFile.add(mnuAddSong);
		mnuFile.add(mnuLoadPlaylist);
		mnuFile.addSeparator();
		mnuFile.add(mnuFileSave);
		mnuFile.add(mnuExport);
		mnuFile.addSeparator();
		mnuFile.add(mnuExit);

		menu.add(mnuFile);
		menu.add(mnuTools);
		this.setJMenuBar(menu);
	}

	protected void loadFromPlayList(File file) {
		List<Song> llist = FileUtils.loadPlaylist(file);
		if (llist != null)
			model.setList(llist);
	}

	protected void copyFiles() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int c = chooser.showOpenDialog(PlaylistPreviewWindow.this);
		if (c == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			boolean errors = false;
			for (Song song : list) {
				try {
					org.apache.commons.io.FileUtils.copyFileToDirectory(
							song.getFile(), file);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error(
							"Error copying file" + song.getFile().getName()
									+ " to " + file.getName(), e1);
					errors = true;
				}
			}
			JOptionPane.showMessageDialog(this,
					errors ? "Some files couldn't be copied successfully"
							: "All Songs copied successfully");
		}

	}

	public void init(List<Song> songList, boolean startPlay,
			SongSelectionProfile profile) {
		setSongs(songList);

		currentSongIndex = 0;
		played = new HashSet<Song>();
		manuallySkipped = new ArrayList<Song>();
		manuallyAdded = new ArrayList<Song>();
		if (profile != null) {
			Iterator<Song> it = profile.getRelPosMap().keySet().iterator();
			while (it.hasNext()) {
				manuallyAdded.add(it.next());
			}
		}

		if (startPlay) {
			playerPanel.setState(PlayerPanel.PlayState.PLAYING);
			play(0);
		}
		if (profile != null)
			setSelectionProfile(profile);
	}

	private void setSelectionProfile(SongSelectionProfile profile) {
		playerPanel.setStrategy(profile.getStrategy());
		playerPanel.setCurrentProfileSettings(profile.getStrategyConfig());
	}

	@Override
	public void dispose() {
		stop();
		super.dispose();
	}

	private void layoutControls() {
		JPanel panel = new JPanel(new MigLayout("insets 6 6 6 6"));
		playerPanel = new PlayerPanel(this);
		panel.add(playerPanel, "span");
		panel.add(scrollPane, "span");
		this.setContentPane(panel);
		this.setVisible(true);
		this.pack();
		this.setSize(360, 400);
		playerPanel.loadLatestSettings();
	}

	public PlayState getCurrentState() {
		return playerPanel.getState();
	}

	private void playNextSong() {
		Song song = getNextSong();
		if (song == null) {
			if (requiresProfileUpdateForInfiniPlay()) {
				selProfile = generateNewProfile();
				List<Song> newSongList;
				try {
					newSongList = StrategyFactory.getPlaylistByStrategy(
							selProfile.getStrategy(), selProfile,
							selProfile.getStrategyConfig(), false, false, list);
					if (newSongList.isEmpty()) {
						// couldn't generate songs. inform the user to try other
						// strategies and update meta data
						JOptionPane
								.showMessageDialog(
										this,
										"<html>Oops! Lysty was unable to find more songs to go with the playlist. <br>You can try the following:<br><br> 1). Use a different fill method. <br><br> 2). This could be due to inadequate metadata for songs. <br>You can fix it by choosing <i>tools->Edit Metadata</i><br><br> 3). Make sure songs are indexed. You can run the indexer by choosing <i>tools->index</i><br><br></html>",
										"Unable to generate more songs",
										JOptionPane.INFORMATION_MESSAGE);
					}
					for (Song s : newSongList) {
						model.addSong(s.getFile(), model.getList().size());
					}
					song = getNextSong();
					if (song != null) {
						currentSongIndex = list.indexOf(song);
						play(0);
					}
				} catch (Exception e) {
					logger.error("Error creating playlist", e);
				}
			}
		} else {
			currentSongIndex = list.indexOf(song);
			play(0);
		}
	}

	/**
	 * Algorithm: Consider the "lastNtoCheck" songs in the current playlist.
	 * take it as baselist. create a partials list that is of length
	 * Math.max(lastNtoCheck,genListSize) add the manually added songs in the
	 * lastNtoCheck to the partials first. the position those songs are added to
	 * are such that the last manually added song goes to the 0th position in
	 * the partials (i.e: rotate the baselist such that last manually added
	 * comes to 0th pos) If less than 50% of the genlist size is filled, add
	 * more randomly chosen songs from the baselist. do the same rotation for
	 * their positions as well. Take partials.sublist(0,genlistsize) and take
	 * that as the relative position map to pass to the fill strategy
	 * 
	 * @return
	 */
	private SongSelectionProfile generateNewProfile() {
		SongSelectionProfile profile = new SongSelectionProfile();

		String genListSizeStr = AppSettingsManager
				.getProperty(AppSettingsManager.INFINIPLAY_GENLIST_SIZE);
		int genListSize = DEFAULT_GENLIST_SIZE;
		if (Utils.stringNotNullOrEmpty(genListSizeStr)) {
			genListSize = Integer.parseInt(genListSizeStr);
		}
		profile.setSize(genListSize);
		profile.setSizeType(SongSelectionProfile.SIZE_TYPE_LENGTH);

		selProfile = new SongSelectionProfile();
		PlaylistGenerator strategy = playerPanel.getCurrentStrategy();
		selProfile.setStrategy(strategy);

		StrategyConfiguration currentStrategySettings = playerPanel
				.getCurrentStrategySettings();
		if (currentStrategySettings == null)
			currentStrategySettings = StrategyFactory
					.getDefaultOrLastSettings(strategy);
		selProfile.setStrategyConfig(currentStrategySettings);

		profile.setStrategy(selProfile.getStrategy());
		profile.setStrategyConfig(selProfile.getStrategyConfig());
		int infiniPlayLastNtoCheck = DEFAULT_INFINIPLAY_LAST_N_TO_CHECK;
		String infiniPlayLastNStr = AppSettingsManager
				.getProperty(AppSettingsManager.INFINIPLAY_LAST_N_TO_CHECK);
		if (Utils.stringNotNullOrEmpty(infiniPlayLastNStr)) {
			infiniPlayLastNtoCheck = Integer.parseInt(infiniPlayLastNStr);
		}

		List<Song> baseList = list.subList(
				Math.max(0, list.size() - infiniPlayLastNtoCheck), list.size());
		int partialsLen = Math.max(infiniPlayLastNtoCheck, genListSize);
		List<Song> partials = new ArrayList<Song>(partialsLen);

		for (int i = 0; i < partialsLen; i++) {
			partials.add(null); // nullfill
		}
		int addedCnt = 0;
		int distFromEndToLastManualAdd = 0;
		for (int i = baseList.size() - 1; i >= 0; i--) {// find dist to last
														// manually added song
														// from the end of list
			if (manuallyAdded.contains(baseList.get(i))) {
				distFromEndToLastManualAdd = baseList.size() - i;
				break;
			}
		}
		for (int i = 0; i < baseList.size(); i++) {
			if (manuallyAdded.contains(baseList.get(i))) { // add the manually
															// addeds
				partials.set(
						(i + distFromEndToLastManualAdd) % baseList.size(),
						baseList.get(i));
				addedCnt++;
			}
		}
		int tries = 0;
		int random;
		while (addedCnt < genListSize / 2 && tries < genListSize) {
			// need to add more random chosens from the last N
			tries++;
			random = (int) (Math.random() * baseList.size());
			if (partials.contains(baseList.get(random))) {
				continue;
			} else {
				partials.set(
						(random + distFromEndToLastManualAdd) % baseList.size(),
						baseList.get(random));
				addedCnt++;
			}
		}
		profile.setRelPosMap(partials.subList(0, genListSize));
		return profile;
	}

	private boolean requiresProfileUpdateForInfiniPlay() {
		if (!playerPanel.getIsInfiniPlay())
			return false;
		// TODO Auto-generated method stub
		if (currentSongIndex + 1 >= list.size())
			return true;
		return false;
	}

	private Song getNextSong() {
		if (isRandomized) {
			int rand;
			int tries = 0;
			while (true) {
				rand = (int) (Math.random() * list.size());
				currentSongIndex = rand;
				if (tries >= list.size()) {
					return null;
				}
				if (played.contains(list.get(rand))) {
					tries++;
				} else {
					return list.get(rand);
				}
			}
		} else {
			if (list.size() <= currentSongIndex + 1)
				return null;

			currentSongIndex++;
			return list.get(currentSongIndex);
		}
	}

	private void setSongs(List<Song> songList) {
		list = songList;
		model.setList(list);
	}

	@Override
	public void play(int playFrom) {
		playSong(currentSongIndex, playFrom);
	}

	private void playSong(int index, int playFrom) {
		if (list.isEmpty())
			return;
		final Song song = list.get(index);
		playerPanel.setPausedOnFrame(playFrom);
		played.add(song);
		table.setRowSelectionInterval(index, index);
		try {
			SongPlayer.getInstance().play(song, playFrom, playbackListener);
			playerPanel.setState(PlayState.PLAYING);
			playerPanel.setCurrentSong(song, playFrom);
		} catch (Exception e) {
			logger.error("Error playing song: " + song.getFile().getName(), e);
			JOptionPane.showMessageDialog(this, "Cannot play file: "
					+ song.getFile().getName());
			playerPanel.setState(PlayerPanel.PlayState.STOPPED);
		}
	}

	@Override
	public void pause() {
		SongPlayer.getInstance().pause();
	}

	@Override
	public void stop() {
		playerPanel.setState(PlayerPanel.PlayState.STOPPED);
		playerPanel.setCurrentSongProgress(0);
		SongPlayer.getInstance().stop();
	}

	@Override
	public void next() {
		manuallySkipped.add(list.get(currentSongIndex));
		playNextSong();
	}

	@Override
	public void prev() {
		currentSongIndex--;
		if (currentSongIndex < 0) {
			currentSongIndex = list.size() - 1;
		}
		play(0);
	}

	@Override
	public void setInfinyPlay(boolean isInfini) {
		if (isInfini) {
			List<Song> allSongs = DBHandler.getInstance().getAllSongs();
			if (allSongs == null || allSongs.size() == 0) {
				String[] options = new String[] { "Yes", "No" };
				// no songs in the index
				int choice = JOptionPane
						.showOptionDialog(
								this,
								"<html>There are no songs currently indexed in the Lysty DB.<br> You need to run the indexer for <i><b>InfiniPlay</b></i> to work.<br><br> Would you like to index songs now?</html>",
								"Indexer Empty", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								JOptionPane.YES_OPTION);
				if (choice == JOptionPane.YES_OPTION) {
					IndexerWindow.getInstance().setVisible(true);
					IndexerWindow.getInstance().setLocationRelativeTo(this);
				}
			}
		}
	}

	public void cancelTimer() {
		if (timer != null) {
			timer.cancel();
		}
	}

	@Override
	public void setTimer(int time) {
		if (timer != null)
			timer.cancel();

		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					Utils.shutdown(1);
				} catch (IOException e) {
					logger.error("Error shutting down", e);
				}
			}
		}, time);

	}

	@Override
	public void setRandomize(boolean isRandom) {
		isRandomized = isRandom;
	}

	public void addSongNext(Song song) {
		int pos = 0;
		if (list == null) {
			list = new ArrayList<Song>();
			currentSongIndex = 0;
		}
		if (manuallyAdded == null)
			manuallyAdded = new ArrayList<Song>();

		if (list.isEmpty()) {
			pos = 0;
		} else {
			pos = currentSongIndex + 1;
		}
		try {
			model.addSong(song.getFile(), pos);
			manuallyAdded.add(song);
			boolean remAutoAdds = "true"
					.equalsIgnoreCase(AppSettingsManager
							.getProperty(AppSettingsManager.REM_UNPLAYED_AUTOGENS_ON_MANUAL_ADD));
			if (remAutoAdds) {
				List<Song> toRem = new ArrayList<Song>();
				for (int i = pos + 1; i < list.size(); i++) {
					if (!manuallyAdded.contains(list.get(i))) {
						// autoadded
						toRem.add(list.get(i));
					}
				}
				for (Song remSong : toRem) {
					list.remove(remSong);
				}
			}
		} catch (SongNotIndexedException e) {
			// never reaches since playlistpreview window doesn't expect
			// file to be indexed
			logger.error("Song add exception in playlist preview window", e);
		}
		return;
	}

	public void enqueueSong(Song song) {
		try {
			model.addSong(song.getFile(), model.getList().size());
			manuallyAdded.add(song);
		} catch (SongNotIndexedException e) {
			// never reaches since playlistpreview window doesn't expect
			// file to be indexed
			logger.error("Song add exception in playlist preview window", e);
		}
	}

}