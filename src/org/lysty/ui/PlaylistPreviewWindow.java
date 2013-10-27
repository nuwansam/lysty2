package org.lysty.ui;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.lysty.core.SongPlayer;
import org.lysty.dao.Song;
import org.lysty.players.AbstractPlayer;
import org.lysty.players.PlayEvent;
import org.lysty.players.PlaybackListener;
import org.lysty.players.PlayerManager;
import org.lysty.ui.PlayerPanel.PlayState;
import org.lysty.ui.exception.SongNotIndexedException;
import org.lysty.ui.exception.SongPlayException;
import org.lysty.util.FileUtils;

public class PlaylistPreviewWindow extends LFrame implements PlayPanelListener {

	private List<Song> list;
	private PlaylistModel model;
	private int currentSongIndex;
	private Set<Song> played;
	private JScrollPane scrollPane;
	private JTable table;
	private PlayerPanel playerPanel;
	private boolean isRandomized;
	static Logger logger = Logger.getLogger(PlaylistPreviewWindow.class);

	public PlaylistPreviewWindow(List<Song> songList, boolean startPlay) {
		super("Preview Playlist");
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

		mnuFile.add(mnuFileSave);
		menu.add(mnuFile);
		this.setJMenuBar(menu);
		currentSongIndex = 0;
		played = new HashSet<Song>();
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
		this.setSize(300, 400);

	}

	private void playNextSong() {
		final Song song = getNextSong();
		currentSongIndex = list.indexOf(song);
		play(0);
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
			currentSongIndex++;
			if (list.size() <= currentSongIndex)
				return null;
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
		playerPanel.setCurrentSong(song);
		SongPlayer.getInstance().play(song, playFrom, new PlaybackListener() {

			@Override
			public void getNotification(PlayEvent event) {
				if (event.getEventType() == PlayEvent.EventType.SONG_ENDED) {
					// previous song has ended;
					playNextSong();
				} else if (event.getEventType() == PlayEvent.EventType.PLAY_EXCEPTION) {
					JOptionPane.showMessageDialog(PlaylistPreviewWindow.this,
							"Error playing song: " + song.getName());
				} else if (event.getEventType() == PlayEvent.EventType.SONG_STOPPED) {
					playerPanel.setPausedOnFrame(event.getFrame());
				}
			}
		});

	}

	@Override
	public void pause() {
		SongPlayer.getInstance().pause();
	}

	@Override
	public void stop() {
		SongPlayer.getInstance().stop();
	}

	@Override
	public void next() {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimer(int time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRandomize(boolean isRandom) {
		isRandomized = isRandom;
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
			Song song = new Song();
			song.setFile(file);
			list.add(position, song);
			fireTableDataChanged();
		}

	}

}
