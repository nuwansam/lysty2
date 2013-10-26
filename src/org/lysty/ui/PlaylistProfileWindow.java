package org.lysty.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.lysty.core.PlaylistGenerator;
import org.lysty.core.PropertyManager;
import org.lysty.core.StrategyFactory;
import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.exceptions.StrategyInitiationException;
import org.lysty.strategies.StrategyConfiguration;
import org.lysty.ui.model.PlaylistProfileModel;
import org.lysty.util.FileUtils;

public class PlaylistProfileWindow extends LFrame {

	private static final String SIZE_TYPE_LENGTH = "Length";
	private static final String SIZE_TYPE_TIME = "Time";
	JTable table;
	PlaylistProfileModel playlistModel;
	protected JPopupMenu tablePopup;
	private JSpinner spnSize;
	private JComboBox<String> cmbSizeType;
	private JComboBox cmbStrategy;
	private StrategySettingsWindow strategySettingsWindow;

	private StrategyConfiguration currentStrategySettings;
	private PlaylistGenerator currentStrategy;
	JScrollPane scroller;
	private JButton btnFillPlay;
	private JButton btnSettings;
	private Logger logger = Logger.getLogger(PlaylistProfileWindow.class);

	public PlaylistProfileWindow(String title) {
		super(title);
		createUI();
		createControlPanel();
		table = new JTable();
		table.setTableHeader(null);
		scroller.setViewportView(table);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		strategySettingsWindow = StrategySettingsWindow.getInstance();
		strategySettingsWindow.setProfileWindow(this);
		// Create the drag and drop listener
		TableDragDropListener myDragDropListener = new TableDragDropListener(
				table);

		playlistModel = new PlaylistProfileModel(
				Integer.parseInt(PropertyManager
						.getProperty(PropertyManager.DEF_PLAYLIST_LEN)));
		table.setModel(playlistModel);
		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);
		table.setTransferHandler(new TableRowTransferHandler(table));

		playlistModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				spnSize.setValue(playlistModel.getRowCount());
			}
		});
		table.setFillsViewportHeight(true);
		TableColumnModel colModel = table.getColumnModel();
		colModel.getColumn(0).setPreferredWidth(Integer.MAX_VALUE);
		table.setDragEnabled(true);
		table.addMouseListener(new MouseAdapter() {
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
					tablePopup = new JPopupMenu();
					JMenuItem mnuRem = new JMenuItem(new AbstractAction(
							"Remove") {

						@Override
						public void actionPerformed(ActionEvent e) {
							playlistModel.removeRow(rowindex);
						}
					});
					tablePopup.add(mnuRem);
					tablePopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		// Connect the label with a drag and drop listener
		new DropTarget(table, myDragDropListener);
		this.setJMenuBar(createMenu());
		// Add the label to the content

		// Show the frame
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	/**
	 * Generated code for UI creation
	 */
	private void createUI() {
		setBounds(100, 100, 330, 540);
		getContentPane().setLayout(null);
		getContentPane().setLayout(null);

		JPanel pnlControl = new JPanel();
		pnlControl.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0,
				0, 0)));
		pnlControl.setBounds(10, 6, 300, 35);
		getContentPane().add(pnlControl);
		pnlControl.setLayout(new MigLayout("",
				"[19px][29px][][46px][][][][][28px][][]", "[20px]"));

		JLabel lblSize = new JLabel("Size");
		pnlControl.add(lblSize, "cell 0 0,alignx left,aligny center");

		spnSize = new JSpinner();
		pnlControl.add(spnSize, "cell 1 0 2 1,alignx left,aligny top");

		JLabel lblSizeType = new JLabel("Size Type");
		pnlControl.add(lblSizeType, "cell 3 0,alignx left,aligny center");

		cmbSizeType = new JComboBox();
		pnlControl.add(cmbSizeType, "cell 4 0 2 1,alignx left,aligny top");

		btnFillPlay = new JButton("Fill & Play");
		btnFillPlay.setFont(new Font("Tahoma", Font.BOLD, 12));
		pnlControl.add(btnFillPlay, "cell 10 0,alignx right");

		JPanel pnlStrategy = new JPanel();
		pnlStrategy.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0,
				0, 0)));
		pnlStrategy.setBounds(10, 46, 300, 35);
		getContentPane().add(pnlStrategy);
		pnlStrategy.setLayout(new MigLayout("", "[][grow]", "[]"));

		JLabel lblFillMethod = new JLabel("Fill Method");
		pnlStrategy.add(lblFillMethod, "cell 0 0,alignx trailing");

		cmbStrategy = new JComboBox();
		pnlStrategy.add(cmbStrategy, "flowx,cell 1 0,alignx left");

		btnSettings = new JButton("Settings...");
		pnlStrategy.add(btnSettings, "cell 1 0");

		scroller = new JScrollPane();
		scroller.setBounds(10, 92, 300, 380);
		getContentPane().add(scroller);

	}

	public void setSize(int size) {
		spnSize.setValue(size);
	}

	private void createControlPanel() {
		cmbSizeType.addItem(SIZE_TYPE_LENGTH);
		cmbSizeType.addItem(SIZE_TYPE_TIME);

		int defLen = Integer.parseInt(PropertyManager
				.getProperty(PropertyManager.DEF_PLAYLIST_LEN));
		spnSize.setValue(defLen);
		spnSize.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				int newSize = PlaylistProfileWindow.this.playlistModel
						.updateSize((Integer) spnSize.getValue());
				spnSize.setValue(newSize);
			}

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub

			}
		});

		spnSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				int newSize = PlaylistProfileWindow.this.playlistModel
						.updateSize((Integer) spnSize.getValue());
				spnSize.setValue(newSize);
			}
		});

		List<PlaylistGenerator> list = StrategyFactory.getAllStrategies();
		cmbStrategy.setModel(new DefaultComboBoxModel<PlaylistGenerator>(list
				.toArray(new PlaylistGenerator[list.size()])));

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

		cmbStrategy.setSelectedIndex(0);

		btnSettings.setAction(new AbstractAction("Settings...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsFrame();
			}
		});

		btnFillPlay.setAction(new AbstractAction("Fill & Play") {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					generatePlayList();
				} catch (Exception e1) {
					logger.error("Error generating playlist", e1);
				}
			}
		});
		onStrategySelectionChange((PlaylistGenerator) cmbStrategy.getItemAt(0));
	}

	protected void onStrategySelectionChange(PlaylistGenerator newStrategy) {
		PlaylistProfileWindow.this.currentStrategy = newStrategy;
		PlaylistProfileWindow.this.currentStrategySettings = null;
	}

	protected void showSettingsFrame() {
		PlaylistGenerator strategy = (PlaylistGenerator) cmbStrategy
				.getSelectedItem();
		strategySettingsWindow.createUi(strategy);
	}

	private JMenuBar createMenu() {
		JMenuBar menu = new JMenuBar();

		JMenu mnuFile = new JMenu("File");
		JMenuItem mnuFileNew = new JMenuItem(new AbstractAction(
				"New Selection Profile") {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isCancel = saveOnRequest();
				if (!isCancel)
					newSelectionProfile();
			}
		});

		JMenuItem mnuFileSaveSP = new JMenuItem(new AbstractAction(
				"Save Selection Profile") {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveSelProfile();
			}
		});

		JMenuItem mnuFileSavePL = new JMenuItem(new AbstractAction(
				"Save Playlist") {

			@Override
			public void actionPerformed(ActionEvent e) {
				savePlaylist();
			}
		});

		JMenuItem mnuFileLoadSP = new JMenuItem(new AbstractAction(
				"Load Selection Profile") {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isCancel = saveOnRequest();
				if (!isCancel)
					loadSelProfile();
			}
		});

		JMenuItem mnuFileExit = new JMenuItem(new AbstractAction("Exit") {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (playlistModel.isEdited()) {
					boolean isCancel = saveOnRequest();
					if (!isCancel)
						exit();
				}
				exit();
			}
		});

		mnuFile.add(mnuFileNew);
		mnuFile.add(mnuFileLoadSP);
		mnuFile.add(mnuFileSaveSP);
		mnuFile.add(mnuFileSavePL);
		mnuFile.add(mnuFileExit);
		menu.add(mnuFile);

		JMenu mnuTools = new JMenu("Tools");
		JMenuItem mnuToolsIndex = new JMenuItem(new AbstractAction("Index...") {

			@Override
			public void actionPerformed(ActionEvent e) {
				showIndexDialog();
			}
		});
		mnuTools.add(mnuToolsIndex);

		JMenuItem mnuToolsGenerate = new JMenuItem(new AbstractAction("Fill") {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					generatePlayList();
				} catch (Exception e1) {
					logger.error("Error generating playlist", e1);
				}
			}
		});
		mnuTools.add(mnuToolsGenerate);

		menu.add(mnuFile);
		menu.add(mnuTools);
		return menu;
	}

	protected void generatePlayList() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			StrategyInitiationException {
		StrategyConfiguration config = (currentStrategySettings == null) ? StrategyFactory
				.getDefaultOrLastSettings(currentStrategy)
				: currentStrategySettings;
		List<Song> list = StrategyFactory.getPlaylistByStrategy(
				currentStrategy, playlistModel.getSelProfile(), config);
		showPlaylist(list);
	}

	private void showPlaylist(List<Song> list) {
		PlaylistPreviewWindow win = new PlaylistPreviewWindow(list, true);
	}

	protected void showIndexDialog() {
		IndexerWindow.getInstance().setVisible(true);
		IndexerWindow.getInstance().setLocationRelativeTo(this);
	}

	protected void exit() {
		this.dispose();
	}

	/**
	 * Prompt user for save if required.
	 * 
	 * @return whether user chose to cancel the operation.
	 */
	protected boolean saveOnRequest() {
		if (playlistModel.isEdited()) {
			int n = JOptionPane.showOptionDialog(PlaylistProfileWindow.this,
					"Do you want to save the current selection profile?",
					"Save", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes",
							"No", "Cancel" }, "Yes");
			if (n == 0) { // save first
				saveSelProfile();
				return false;
			} else if (n == 2) {
				return true;
			}
		}
		return false;
	}

	protected void loadSelProfile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(FileUtils.selProfileFileFilter);
		int c = chooser.showOpenDialog(this);
		if (c != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = chooser.getSelectedFile();
		SongSelectionProfile selProfile = FileUtils.loadSelectionProfile(file);
		playlistModel.loadFromSelProfile(selProfile);
	}

	/**
	 * New Selection profile
	 */
	protected void newSelectionProfile() {
		playlistModel = new PlaylistProfileModel(
				Integer.parseInt(PropertyManager
						.getProperty(PropertyManager.DEF_PLAYLIST_LEN)));
		table.setModel(playlistModel);
	}

	/**
	 * Save the selection profile
	 * 
	 * @return success
	 */
	protected boolean saveSelProfile() {
		// TODO Auto-generated method stub
		SongSelectionProfile selProfile = playlistModel.getSelProfile();
		if (selProfile.getFile() == null) {
			// no file set. SaveAs...
			JFileChooser chooser = new JFileChooser();
			int c = chooser.showSaveDialog(this);
			if (c != JFileChooser.APPROVE_OPTION) {
				return false;
			}
			File file = chooser.getSelectedFile();
			try {
				FileUtils.writeXml(file, selProfile.getXml());
				playlistModel.saved();
				return true;
			} catch (IOException e) {
				logger.error("Error saving selection profile", e);
				return false;
			}
		} else {
			// write to existing file
			try {
				boolean success = FileUtils.saveSelectionProfile(selProfile,
						selProfile.getFile());
				if (success)
					playlistModel.saved();
				return success;
			} catch (IOException e) {
				logger.error("", e);
				return false;
			}
		}
	}

	protected void savePlaylist() {
	}

	public void setCurrentProfileSettings(StrategyConfiguration strategySettings) {
		this.currentStrategySettings = strategySettings;
		StrategyFactory.updateLastSettings(currentStrategy, strategySettings);
	}

}
