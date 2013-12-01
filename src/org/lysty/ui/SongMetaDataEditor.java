package org.lysty.ui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.lysty.core.ExtractorManager;
import org.lysty.dao.Song;
import org.lysty.db.DBHandler;
import org.lysty.util.Utils;

public class SongMetaDataEditor extends LFrame {

	private Song song;
	private JTable table;
	private JButton btnCommit;
	private JButton btnCancel;
	private MetaDataModel model;

	public SongMetaDataEditor(String title) {
		super("View / Edit Meta Data");
	}

	public SongMetaDataEditor() {
		this("");
	}

	public void setSong(Song song) {
		this.song = song;
	}

	public void init(Song song) {
		setSong(song);
		createUI();
		LayoutUI();
		this.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				List<Modification> changes = model.getChanges();
				if (!changes.isEmpty()) {
					String[] options = new String[] { "Yes", "No" };
					int choice = JOptionPane
							.showOptionDialog(
									SongMetaDataEditor.this,
									"There are unsaved modifications. Would you like to save them?",
									"Unsaved Modifications",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									options, JOptionPane.YES_OPTION);
					if (choice == JOptionPane.YES_OPTION) {
						DBHandler.getInstance().applyModifications(
								model.getChanges(), null);
					}
				}
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void LayoutUI() {
		JPanel pnl = new JPanel(new MigLayout("", "", ""));

		JPanel controlPanel = new JPanel(new MigLayout("", "push[][]", "[]"));
		controlPanel.add(btnCommit, "flowx,push,align right");
		controlPanel.add(btnCancel, "flowx");

		JScrollPane scroller = new JScrollPane(table);
		pnl.add(scroller, "span");
		pnl.add(controlPanel);
		setContentPane(pnl);
		pack();
		setVisible(true);
	}

	private void createUI() {
		table = new JTable();
		model = new MetaDataModel(song);
		table.setModel(model);
		btnCommit = new JButton(new AbstractAction("Commit Changes") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				DBHandler.getInstance().applyModifications(model.getChanges(),
						null);
				SongMetaDataEditor.this.dispose();
			}
		});
		btnCancel = new JButton(new AbstractAction("Cancel") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				SongMetaDataEditor.this.dispose();
			}
		});

	}

	class MetaDataModel extends DefaultTableModel {

		private Song song;
		private List<String> attribList;
		private List<Modification> changes;

		public List<Modification> getChanges() {
			return changes;
		}

		public MetaDataModel(Song song) {
			Song song2 = DBHandler.getInstance().getSong(song.getFile());
			changes = new ArrayList<Modification>();
			if (song2 == null) {
				changes.add(new Modification(song, null, null, null));
				song2 = song;
			}
			this.song = song2;
			attribList = ExtractorManager.getSupportedFeatures();
			Collections.sort(attribList);
			fireTableDataChanged();
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0)
				return "Feature";
			if (col == 1)
				return "Value";
			return "";
		}

		@Override
		public int getRowCount() {
			if (attribList != null)
				return attribList.size();
			return 0;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return attribList.get(row);
			} else if (col == 1) {
				return song.getAttribute(attribList.get(row));
			}
			return "";
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			// TODO Auto-generated method stub
			return col == 1;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			// TODO Auto-generated method stub
			if (col == 1) {
				boolean changed = true;
				String newVal = value.toString();
				String oldVal = song.getAttribute(attribList.get(row));

				if (Utils.stringNotNullOrEmpty(newVal)
						&& Utils.stringNotNullOrEmpty(oldVal)) {
					changed = !newVal.trim().equals(oldVal.trim());
				}
				if (changed) {
					Modification change = new Modification(song,
							attribList.get(row), newVal, oldVal);
					song.setAttribute(attribList.get(row), value.toString());
					changes.add(change);
				}
			}
		}
	}
}
