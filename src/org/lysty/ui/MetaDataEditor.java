package org.lysty.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.lysty.core.ExtractorManager;
import org.lysty.dao.Song;
import org.lysty.db.DBHandler;
import org.lysty.util.Utils;

public class MetaDataEditor extends LFrame {

	private static MetaDataEditor self = null;

	public static MetaDataEditor getInstance() {
		if (self == null) {
			self = new MetaDataEditor("Meta Data Editor");
		}
		return self;
	}

	private List<Song> songs;

	private MetaDataEditor(String title) {
		super(title);
	}

	public void createUI() {
		List<String> features = features = ExtractorManager
				.getSupportedFeatures();
		songs = DBHandler.getInstance().getSongs(null);
		final LTableModel model = new LTableModel(features, songs);
		model.setCurrentFolder(null);
		JTable table = new JTable();
		JScrollPane scroller = new JScrollPane(table);
		table.setModel(model);
		final Border eBorder = new EmptyBorder(5, 2, 5, 2);
		table.setDefaultRenderer(File.class, new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JLabel label;
				if (value instanceof File) {
					String name = ((File) value).getName();
					if (!Utils.stringNotNullOrEmpty(name)) {
						name = ((File) value).getAbsolutePath();
					}
					label = new JLabel(name, Utils.getIcon(((File) value)
							.isDirectory() ? ResourceConstants.FOLDER_ICON
							: ResourceConstants.SONG_ICON), SwingConstants.LEFT);
					label.setBorder(eBorder);
					if (((File) value).isFile()) {
						int index = songs.indexOf(new Song((File) value));
						if (index < 0) {
							// not indexed;
							label.setForeground(Color.GRAY);
						}
					}
					return label;
				}
				label = new JLabel(value.toString());
				return label;
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					LTableModel lModel = (LTableModel) target.getModel();
					File file = (File) lModel.getValueAt(row, 0);
					if (file.getName().equalsIgnoreCase("..")) {
						if (lModel.getCurrentFolder() != null) {
							lModel.setCurrentFolder(lModel.getCurrentFolder()
									.getParentFile());
						}
					} else if (file.isDirectory()) {
						lModel.setCurrentFolder(file);
					}
				}
			}
		});
		TableColumnModel cModel = table.getColumnModel();
		cModel.getColumn(0).setMinWidth(100);

		JButton btnCommit = new JButton(new AbstractAction("Commit Changes") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final List<Modification> changes = model.getChanges();
				final ProgressMonitor monitor = new ProgressMonitor(
						MetaDataEditor.this, "Committing Changes",
						"Writing the changes to the DB", 0, changes.size());
				new Thread() {
					@Override
					public void run() {
						DBHandler.getInstance().applyModifications(changes,
								monitor);
						cleanChanges();
						MetaDataEditor.this.setVisible(false);
					}

				}.start();
			}
		});

		JButton btnCancel = new JButton(new AbstractAction("Cancel") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				disposeChanges();
				MetaDataEditor.this.setVisible(false);
			}
		});

		table.setPreferredScrollableViewportSize(new Dimension(1000, 500));
		JPanel panel = new JPanel(new MigLayout());
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(scroller, "north");
		panel.add(btnCommit, "right align");
		panel.add(btnCancel, "right align");
		this.setContentPane(panel);

		this.setVisible(true);
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.pack();
	}

	private void cleanChanges() {

	}

	protected void disposeChanges() {
		songs = DBHandler.getInstance().getSongs(null);
	}

	class LTableModel extends DefaultTableModel {

		private File currentFolder;
		private List<String> features;
		private File[] currentFiles;
		private FileFilter fileFilter;
		private List<Modification> changeList;
		private List<Song> songs;

		public LTableModel(List<String> features, List<Song> songs) {
			this.features = features;
			currentFiles = new File[0];
			changeList = new ArrayList<Modification>();
			this.songs = songs;
			final String[] formats = ExtractorManager.getSupportedFormats();
			fileFilter = new FileFilter() {

				@Override
				public boolean accept(File file) {
					if (file.isDirectory())
						return true;
					for (String format : formats) {
						if (file.getName().toLowerCase().endsWith("." + format))
							return true;
					}
					return false;
				}
			};

		}

		public File getCurrentFolder() {
			return currentFolder;
		}

		public void setCurrentFolder(File folder) {
			this.currentFolder = folder;
			if (folder == null) {
				currentFiles = File.listRoots();
			} else {
				currentFiles = folder.listFiles(fileFilter);
			}
			sortFiles();
			fireTableDataChanged();
		}

		private void sortFiles() {
			Comparator<? super File> comparator = new Comparator<File>() {

				@Override
				public int compare(File o1, File o2) {
					if ((o1.isDirectory() && o2.isDirectory())
							|| (!o1.isDirectory() && !o2.isDirectory())) {
						return o1.getAbsolutePath().compareTo(
								o2.getAbsolutePath());
					}
					if (o1.isDirectory())
						return -1;
					if (o2.isDirectory())
						return 1;
					return 0;
				}
			};
			Arrays.sort(currentFiles, comparator);
		}

		public void setFeatureList(List<String> features) {
			this.features = features;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0)
				return File.class;
			return String.class;
		}

		@Override
		public int getColumnCount() {
			if (features == null)
				return 1;
			return features.size() + 1;
		}

		@Override
		public String getColumnName(int arg0) {
			if (arg0 == 0)
				return "File";
			return features.get(arg0 - 1);
		}

		@Override
		public int getRowCount() {
			if (currentFiles == null)
				return 1;
			return currentFiles.length + 1;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0 && row == 0) {
				return new File("..");
			}
			if (row == 0)
				return "";
			if (col == 0) {
				return currentFiles[row - 1];
			}
			Song song = getSong(currentFiles[row - 1]);
			String feature = features.get(col - 1);
			if (song == null)
				return "";
			return song.getAttribute(feature);
		}

		@Override
		public boolean isCellEditable(int row, int col) {

			return (col != 0 && row != 0);
		}

		@Override
		public void setValueAt(Object val, int row, int col) {
			File file = currentFiles[row - 1];
			String feature = features.get(col - 1);
			if (file.isDirectory()) {
				setAttributeToChildren(file, feature, val.toString());
			} else {
				setFeature(file, feature, val.toString());
			}
			fireTableDataChanged();
		}

		private void setAttributeToChildren(File file, String feature,
				String value) {
			if (file.isDirectory()) {
				File[] files = file.listFiles(fileFilter);
				if (files != null) {
					for (File f : files) {
						setAttributeToChildren(f, feature, value);
					}
				}
			}
			boolean isEmpty = false;
			Song song = getSong(file);
			if (song == null
					|| !Utils.stringNotNullOrEmpty(song.getAttribute(feature)))
				isEmpty = true;

			if (isEmpty)
				setFeature(file, feature, value);
		}

		public List<Modification> getChanges() {
			return changeList;
		}

		private Song getSong(File file) {
			Song song = null;
			int songIndex = songs.indexOf(new Song(file));
			if (songIndex >= 0) {
				song = songs.get(songIndex);
			}
			return song;
		}

		private void setFeature(File file, String feature, String value) {

			Song song = getSong(file);
			String oldValue = null;
			if (song == null) {
				song = new Song(file);
				songs.add(song);
				Modification mod = new Modification(song);
				changeList.add(mod);
			}
			oldValue = song.getAttribute(feature);
			song.setAttribute(feature, value);
			if (!value.equals(oldValue)) {
				Modification change = new Modification(getSong(file), feature,
						value, oldValue);
				changeList.add(change);
			}
		}
	}
}
