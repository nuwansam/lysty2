package org.lysty.ui;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.lysty.core.PlaylistGenerator;
import org.lysty.core.SongPlayer;
import org.lysty.core.StrategyFactory;
import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.db.DBHandler;
import org.lysty.players.PlayEvent;
import org.lysty.players.PlaybackListener;
import org.lysty.strategies.StrategyConfiguration;
import org.lysty.strategies.random.RandomStrategy;
import org.lysty.ui.exception.SongNotIndexedException;
import org.lysty.util.FileUtils;

public class PlaylistPreviewWindow extends LFrame implements PlayPanelListener {

	private static final int INFINI_PLAY_GENLIST_SIZE = 5;
	private static final int INFINI_PLAY_LAST_N_TOCHECK = 8;
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
		init(new ArrayList<Song>(), false, null);
		playbackListener = new PlaybackListener() {

			@Override
			public void getNotification(PlayEvent event) {
				if (event.getEventType() == PlayEvent.EventType.SONG_ENDED) {
					// previous song has ended;
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
		WindowManager.getInstance().registerWindow(this);
	}

	public void init(List<Song> songList, boolean startPlay,
			SongSelectionProfile profile) {
		this.selProfile = profile;
		setSongs(songList);
		layoutControls();
		JMenuBar menu = new JMenuBar();
		JMenu mnuFile = new JMenu("File");
		JMenuItem mnuFileSave = new JMenuItem(new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int c = chooser.showSaveDialog(PlaylistPreviewWindow.this);
				if (c != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File file = chooser.getSelectedFile();
				try {
					boolean success = FileUtils.savePlaylist(list, file);
				} catch (IOException e1) {
					logger.error(
							"Error saving playlist to: "
									+ file.getAbsolutePath(), e1);
				}

			}
		});

		JMenu mnuTools = new JMenu("Tools");
		JMenuItem mnuEditMetaData = new JMenuItem(new AbstractAction(
				"Edit MetaData") {

			@Override
			public void actionPerformed(ActionEvent e) {
				MetaDataEditor.getInstance().createUI();
				MetaDataEditor.getInstance().setVisible(true);
			}
		});

		JMenuItem mnuPPEdit = new JMenuItem(new AbstractAction(
				"Open Partial Playlist Editor") {

			@Override
			public void actionPerformed(ActionEvent e) {
				PlaylistProfileWindow.getInstance().setVisible(true);
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
				AppSettingsWindow.getInstance().setVisible(true);
			}
		});

		mnuTools.add(mnuEditMetaData);
		mnuTools.add(mnuPPEdit);
		mnuTools.add(mnuToolsIndex);
		mnuTools.add(mnuToolsSettings);
		mnuFile.add(mnuFileSave);
		menu.add(mnuFile);
		menu.add(mnuTools);
		this.setJMenuBar(menu);

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
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

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
		this.setSize(337, 400);

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

	private SongSelectionProfile generateNewProfile() {
		SongSelectionProfile profile = new SongSelectionProfile();

		profile.setSize(INFINI_PLAY_GENLIST_SIZE);
		profile.setSizeType(SongSelectionProfile.SIZE_TYPE_LENGTH);
		if (selProfile == null) {
			selProfile = new SongSelectionProfile();
			PlaylistGenerator strategy = playerPanel.getCurrentStrategy();
			selProfile.setStrategy(strategy);

			StrategyConfiguration currentStrategySettings = playerPanel
					.getCurrentStrategySettings();
			if (currentStrategySettings == null)
				currentStrategySettings = StrategyFactory
						.getDefaultOrLastSettings(strategy);
			selProfile.setStrategyConfig(currentStrategySettings);

		}
		profile.setStrategy(selProfile.getStrategy());
		profile.setStrategyConfig(selProfile.getStrategyConfig());
		List<Song> baseList = list.subList(
				Math.max(0, list.size() - INFINI_PLAY_LAST_N_TOCHECK),
				list.size());
		List<Song> partials = new ArrayList<Song>(INFINI_PLAY_GENLIST_SIZE);
		for (int i = 0; i < INFINI_PLAY_GENLIST_SIZE; i++) {
			partials.add(null); // nullfill
		}
		int addedCnt = 0;
		for (int i = 0; i < baseList.size(); i++) {
			if (manuallyAdded.contains(baseList.get(i))) { // add the manually
															// addeds
				partials.set(i, baseList.get(i));
				addedCnt++;
			}
		}
		int tries = 0;
		int random;
		while (addedCnt < INFINI_PLAY_GENLIST_SIZE / 2
				&& tries < INFINI_PLAY_GENLIST_SIZE) {
			// need to add more random chosens from the last N
			tries++;
			random = (int) (Math.random() * baseList.size());
			if (partials.contains(baseList.get(random))) {
				continue;
			} else {
				partials.set(Math.min(random, partials.size() - 1),
						baseList.get(random));
				addedCnt++;
			}
		}
		profile.setRelPosMap(partials);
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
				if (tries >= list.size()) {
					return list.get(rand);
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
		table = new JTable();
		model = new PlaylistModel();
		model.setList(list);

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

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					currentSongIndex = row;
					stop();
					playerPanel.setState(PlayerPanel.PlayState.PLAYING);
					playerPanel.setCurrentProgress(0);
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
							model.removeRow(rowindex);
						}
					});
					tablePopup.add(mnuRem);
					tablePopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		table.setTableHeader(null);

	}

	@Override
	public void play(int playFrom) {
		playSong(currentSongIndex, playFrom);
	}

	private void playSong(int index, int playFrom) {
		final Song song = list.get(index);
		playerPanel.setPausedOnFrame(playFrom);
		played.add(song);
		table.setRowSelectionInterval(index, index);
		playerPanel.setCurrentSong(song, playFrom);
		SongPlayer.getInstance().play(song, playFrom, playbackListener);

	}

	@Override
	public void pause() {
		SongPlayer.getInstance().pause();
	}

	@Override
	public void stop() {
		playerPanel.setCurrentProgress(0);
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

	}

	@Override
	public void setTimer(int time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRandomize(boolean isRandom) {
		isRandomized = isRandom;
	}

	public void addSongNext(Song song) {
		int pos = 0;
		if (list.isEmpty()) {
			pos = 0;
		} else {
			pos = currentSongIndex + 1;
		}
		try {
			model.addSong(song.getFile(), pos);
			manuallyAdded.add(song);
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

	class PlaylistModel extends DefaultTableModel implements Reorderable,
			SongDroppable {

		private List<Song> list = new ArrayList<Song>();

		/**
		 * @return the list
		 */
		public List<Song> getList() {
			return list;
		}

		/**
		 * @param list
		 *            the list to set
		 */
		public void setList(List<Song> list) {
			this.list = list;
		}

		@Override
		public int getRowCount() {
			if (list != null)
				return list.size();
			return 0;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public String getColumnName(int columnIndex) {
			// TODO Auto-generated method stub
			return "Song";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			// TODO Auto-generated method stub
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return list.get(rowIndex).getName();
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			list.set(rowIndex, (Song) value);
		}

		@Override
		public void removeRow(int row) {
			list.remove(row);
			fireTableDataChanged();
		}

		@Override
		public void reorder(int fromIndex, int toIndex) {
			Song song = list.get(fromIndex);
			list.remove(fromIndex);
			list.add(toIndex, song);
			fireTableDataChanged();
		}

		@Override
		public void addSong(File file, int position)
				throws SongNotIndexedException {
			Song song = DBHandler.getInstance().getSong(file);
			if (song == null) {
				song = new Song();
				song.setFile(file);
			}
			if (position >= list.size()) {
				list.add(song);
			} else {
				list.add(position, song);
			}
			fireTableDataChanged();
		}

	}

}
