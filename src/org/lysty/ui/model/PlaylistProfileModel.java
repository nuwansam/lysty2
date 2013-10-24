package org.lysty.ui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.DefaultTableModel;

import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.db.DBHandler;
import org.lysty.ui.Reorderable;
import org.lysty.ui.SongDroppable;
import org.lysty.ui.exception.SongNotIndexedException;

public class PlaylistProfileModel extends DefaultTableModel implements
		Reorderable, SongDroppable {

	private static final int DEFAULT_ROW_COUNT = 10;
	SongSelectionProfile selProfile;
	List<Song> songs;
	private boolean isEdited;

	/**
	 * Updates the size to new size. this is a best attempt method. If it is not
	 * possible to update to the given size (if asked to reduce to a size
	 * smaller than the number of songs selected for the list, it does its best
	 * to reduce as much as possible
	 * 
	 * @param newSize
	 *            the new size of the songs list.
	 * @return the actual new size of the list after modification. might not be
	 *         the same as newSize since this is a best attempt algo
	 */
	public int updateSize(int newSize) {
		int rows = songs.size();
		if (newSize == rows) {
			// no change. no need to do anything
			return newSize;
		}
		Map<Song, Integer> blanks = new HashMap<Song, Integer>();
		Song cSong = new Song();
		cSong.setId(-1);// dummy song to signal the beginning of the array
		int cnt = 0;
		int totalBlanks = 0;
		for (int i = 0; i < rows; i++) {
			if (songs.get(i) != null) {
				blanks.put(cSong, cnt);
				cSong = songs.get(i);
				cnt = 0;
			} else if (songs.get(i) == null) {
				cnt++;
				totalBlanks++;
			}
		}
		blanks.put(cSong, cnt); // put the follow up blanks after last song
		int cCnt;
		int rand;
		if (newSize > rows) {
			// size is increased
			int adds = newSize - rows;
			int indexToAddBlank;
			Iterator<Entry<Song, Integer>> it;
			Entry<Song, Integer> entry;
			for (int i = 0; i < adds; i++) {
				cCnt = 0;
				rand = (int) (Math.random() * totalBlanks);
				it = blanks.entrySet().iterator();
				while (it.hasNext()) {
					entry = it.next();
					cCnt += entry.getValue();
					if (rand <= cCnt) {
						if (entry.getKey().getId() == -1) {
							indexToAddBlank = 0;
						} else {
							indexToAddBlank = songs.indexOf(entry.getKey()) + 1;
						}
						if (indexToAddBlank >= songs.size()) {
							songs.add(null);
						} else {
							songs.add(indexToAddBlank, null);
						}
						break;
					}
				}
			}
		} else if (newSize < rows) {
			// size is decreased
			int rems = rows - newSize;
			int indexToRemBlank;
			Iterator<Entry<Song, Integer>> it;
			Entry<Song, Integer> entry;
			for (int i = 0; i < rems; i++) {
				cCnt = 0;
				rand = (int) (Math.random() * totalBlanks);
				it = blanks.entrySet().iterator();
				while (it.hasNext()) {
					entry = it.next();
					cCnt += entry.getValue();
					if (rand <= cCnt) {
						if (entry.getKey().getId() == -1) {
							indexToRemBlank = 0;
						} else {
							indexToRemBlank = songs.indexOf(entry.getKey()) + 1;
						}

						if (indexToRemBlank < songs.size()
								&& songs.get(indexToRemBlank) == null) {
							songs.remove(indexToRemBlank);
							break;
						}
					}
				}
			}
		}
		fireTableDataChanged();
		return songs.size();
	}

	public PlaylistProfileModel(int size) {
		selProfile = new SongSelectionProfile();
		songs = new ArrayList<Song>(size);
		for (int i = 0; i < size; i++)
			songs.add(null);

	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		if (songs == null)
			return 0;
		return songs.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String getColumnName(int colIndex) {
		return "Name";
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

	public Song getSongAt(int rowIndex) {
		return songs.get(rowIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			Song song = songs.get(rowIndex);
			if (song == null)
				return "";
			return song.getName();
		} catch (IndexOutOfBoundsException e) {
			return "";
		}
	}

	public void saved() {
		isEdited = false;
	}

	public boolean isEdited() {
		return isEdited;
	}

	@Override
	public void setValueAt(Object val, int rowIndex, int columnIndex) {
		songs.set(rowIndex, (Song) val);
	}

	public SongSelectionProfile getSelProfile() {
		selProfile.setSize(songs.size());
		selProfile.updateRelPosMap(songs);
		return selProfile;
	}

	public void loadFromSelProfile(SongSelectionProfile profile) {
		selProfile = profile;
		int rows = 10;
		if (selProfile.getSizeType() == selProfile.SIZE_TYPE_LENGTH) {
			rows = selProfile.getSize();
		}
		Map<Song, Integer> map = selProfile.getRelPosMap();
		Iterator<Entry<Song, Integer>> it = map.entrySet().iterator();
		Entry<Song, Integer> entry;
		for (int i = 0; i < rows; i++) {
			songs.add(null);
		}
		while (it.hasNext()) {
			entry = it.next();
			songs.set(entry.getValue(), entry.getKey());
		}
	}

	public void addSong(File file, int row) throws SongNotIndexedException {
		isEdited = true;
		Song song = DBHandler.getInstance().getSong(file);
		while (row >= songs.size()) {
			songs.add(null);
		}
		if (song == null)
			throw new SongNotIndexedException(file);
		if (songs.size() <= row) {
			songs.add(null);
		}
		if (songs.get(row) != null) {
			songs.add(row, song);
		} else {
			songs.set(row, song);
		}
		fireTableDataChanged();
	}

	@Override
	public void removeRow(int row) {
		songs.remove(row);
		fireTableDataChanged();
	}

	@Override
	public void reorder(int fromIndex, int toIndex) {
		Song song = songs.get(fromIndex);
		songs.remove(fromIndex);
		songs.add(toIndex, song);
		fireTableDataChanged();
	}

}
