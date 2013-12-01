package org.lysty.ui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.lysty.dao.Song;
import org.lysty.db.DBHandler;
import org.lysty.ui.Reorderable;
import org.lysty.ui.SongDroppable;
import org.lysty.ui.exception.SongNotIndexedException;

public class PlaylistModel extends DefaultTableModel implements Reorderable,
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
		fireTableDataChanged();
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

	public Song getSongAt(int rowIndex) {
		return list.get(rowIndex);
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
	public void addSong(File file, int position) throws SongNotIndexedException {
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
