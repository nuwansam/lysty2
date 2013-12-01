package org.lysty.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.lysty.core.AppSettingsManager;
import org.lysty.core.PlaylistGenerator;
import org.lysty.core.StrategyFactory;
import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.exceptions.StrategyInitiationException;
import org.lysty.strategies.StrategyConfiguration;
import org.lysty.ui.model.PlaylistProfileModel;
import org.lysty.util.FileUtils;
import org.lysty.util.Utils;

public class PlaylistProfileWindow extends LFrame implements
		StrategySettingsListener {

	private static final String SIZE_TYPE_LENGTH = "Songs";
	private static final String SIZE_TYPE_TIME = "Minutes";
	private static final int DEF_PLAYLIST_LEN = 10;
	JTable table;
	PlaylistProfileModel playlistModel;
	protected JPopupMenu tablePopup;
	private JSpinner spnSize;
	private JComboBox cmbSizeType;
	private JComboBox cmbStrategy;
	private StrategySettingsWindow strategySettingsWindow;
	private JToggleButton chkIsCircular;

	private StrategyConfiguration currentStrategySettings;
	private PlaylistGenerator currentStrategy;
	JScrollPane scroller;
	private JButton btnFillPlay;
	private JButton btnSettings;
	private Logger logger = Logger.getLogger(PlaylistProfileWindow.class);
	private JLabel lblHelp;

	private static PlaylistProfileWindow self = null;

	public static PlaylistProfileWindow getInstance() {
		if (self == null) {
			self = new PlaylistProfileWindow("Lysty Partial Playlist Editor");
		}
		return self;
	}

	private PlaylistProfileWindow(String title) {
		super(title);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				writeSettings();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

		});
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		init();
		loadLastSettings();
	}

	/**
	 * Loads the last settings for each control
	 */
	private void loadLastSettings() {
		spnSize.setValue(Integer.parseInt(AppSettingsManager.getProperty(
				AppSettingsManager.LS_PPL_SIZE, DEF_PLAYLIST_LEN + "")));
		String strategyClass = AppSettingsManager
				.getProperty(AppSettingsManager.LS_FILL_STRATEGY);
		if (Utils.stringNotNullOrEmpty(strategyClass)) {
			PlaylistGenerator strategy = StrategyFactory
					.getStrategyByClassName(strategyClass);
			if (strategy != null)
				cmbStrategy.setSelectedItem(strategy);
		}

		chkIsCircular.setSelected(AppSettingsManager
				.getPropertyAsBoolean(AppSettingsManager.LS_IS_CIRC_PPL));
	}

	private void writeSettings() {
		AppSettingsManager.setProperty(AppSettingsManager.LS_FILL_STRATEGY,
				((PlaylistGenerator) cmbStrategy.getSelectedItem()).getClass()
						.getName());
		AppSettingsManager.setProperty(AppSettingsManager.LS_IS_CIRC_PPL,
				chkIsCircular.isSelected() ? "true" : "false");
		AppSettingsManager.setProperty(AppSettingsManager.LS_PPL_SIZE,
				spnSize.getValue() + "");

	}

	public void init() {
		createControlPanel();
		table = new JTable();
		table.setTableHeader(null);
		scroller = new JScrollPane();
		scroller.setViewportView(table);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		strategySettingsWindow = StrategySettingsWindow.getInstance();
		strategySettingsWindow.setListener(this);
		// Create the drag and drop listener
		TableDragDropListener myDragDropListener = new TableDragDropListener(
				table);

		playlistModel = new PlaylistProfileModel(DEF_PLAYLIST_LEN);
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

		lblHelp = new JLabel(
				"<html><i>Drag and drop songs to desired positions in the list</i></html>");
		// Show the frame
		setToolTips();
		layoutUI();
		this.setVisible(true);
	}

	private void setToolTips() {
		chkIsCircular.setToolTipText("create cirular playlist");
		cmbStrategy
				.setToolTipText("Fill strategy to use for creating the playlist");
		table.setToolTipText("Drag and drop songs to the desired positions in this table");
	}

	private void layoutUI() {
		JPanel pnlControl = new JPanel(new MigLayout("insets 2 2 2 2",
				"[][][][]push[]", "[]"));
		pnlControl.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlControl.add(new JLabel("Size"));
		pnlControl.add(spnSize);
		pnlControl.add(cmbSizeType);
		pnlControl.add(chkIsCircular);
		pnlControl.add(btnFillPlay);

		JPanel pnlStrategy = new JPanel(new MigLayout("insets 2 2 2 2",
				"[][grow][]", "[]"));
		pnlStrategy.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlStrategy.add(new JLabel("Fill Method"));
		pnlStrategy.add(cmbStrategy, "grow");
		pnlStrategy.add(btnSettings);

		JPanel panel = new JPanel(new MigLayout("", "[grow]", "[][][][grow]"));
		panel.add(pnlControl, "cell 0 0, grow");
		panel.add(pnlStrategy, "cell 0 1,grow");
		panel.add(lblHelp, "cell 0 2,center,span");
		panel.add(scroller, "cell 0 3,grow");

		setContentPane(panel);
		pack();
		this.setMinimumSize(new Dimension(330, 400));
		this.setMaximumSize(new Dimension(330, 800));
		this.setSize(new Dimension(330, 450));
		setVisible(true);
	}

	public void setSize(int size) {
		spnSize.setValue(size);
	}

	private void createControlPanel() {
		cmbSizeType = new JComboBox();
		spnSize = new JSpinner();

		cmbSizeType.addItem(SIZE_TYPE_LENGTH);
		// cmbSizeType.addItem(SIZE_TYPE_TIME); //not supported yet

		int defLen = DEF_PLAYLIST_LEN;
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
		cmbStrategy = new JComboBox();
		cmbStrategy.setModel(new DefaultComboBoxModel(list
				.toArray(new PlaylistGenerator[list.size()])));

		cmbStrategy.setRenderer(new ListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList arg0,
					Object arg1, int arg2, boolean arg3, boolean arg4) {
				JLabel lbl = new JLabel(((PlaylistGenerator) arg1)
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

		cmbStrategy.setSelectedIndex(0);

		btnSettings = new JButton(new AbstractAction("") {

			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsFrame();
			}
		});
		btnSettings.setIcon(Utils.getIcon(ResourceConstants.SETTINGS_ICON));

		btnFillPlay = new JButton(new AbstractAction("Fill & Play") {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					generatePlayList(true);
				} catch (Exception e1) {
					logger.error("Error generating playlist", e1);
				}
			}
		});

		chkIsCircular = new JToggleButton(
				Utils.getIcon(ResourceConstants.CIRCULAR_ICON));
		onStrategySelectionChange((PlaylistGenerator) cmbStrategy.getItemAt(0));
	}

	protected void onStrategySelectionChange(PlaylistGenerator newStrategy) {
		PlaylistProfileWindow.this.currentStrategy = newStrategy;
		PlaylistProfileWindow.this.currentStrategySettings = StrategyFactory
				.getDefaultOrLastSettings(newStrategy);

	}

	protected void showSettingsFrame() {
		PlaylistGenerator strategy = (PlaylistGenerator) cmbStrategy
				.getSelectedItem();
		strategySettingsWindow.createUi(strategy, this);
		if (currentStrategySettings != null) {
			strategySettingsWindow.loadSettings(currentStrategySettings);
		}
	}

	private JMenuBar createMenu() {
		JMenuBar menu = new JMenuBar();

		JMenu mnuFile = new JMenu("File");
		JMenuItem mnuFileNew = new JMenuItem(new AbstractAction(
				"New Partial Playlist") {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isCancel = saveOnRequest();
				if (!isCancel)
					newSelectionProfile();
			}
		});

		JMenuItem mnuFileSaveSP = new JMenuItem(new AbstractAction(
				"Save Partial Playlist") {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveSelProfile();
			}
		});

		JMenuItem mnuFileLoadSP = new JMenuItem(new AbstractAction(
				"Load Partial Playlist") {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean isCancel = saveOnRequest();
				if (!isCancel)
					loadSelProfile();
			}
		});

		JMenuItem mnuFileExit = new JMenuItem(new AbstractAction("Close") {

			@Override
			public void actionPerformed(ActionEvent e) {

				close();
			}
		});

		mnuFile.add(mnuFileNew);
		mnuFile.add(mnuFileLoadSP);
		mnuFile.addSeparator();
		mnuFile.add(mnuFileSaveSP);
		mnuFile.addSeparator();
		mnuFile.add(mnuFileExit);
		menu.add(mnuFile);

		menu.add(mnuFile);
		return menu;
	}

	protected void generatePlayList(boolean autoPlay)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, StrategyInitiationException {
		StrategyConfiguration config = (currentStrategySettings == null) ? StrategyFactory
				.getDefaultOrLastSettings(currentStrategy)
				: currentStrategySettings;
		List<Song> list = StrategyFactory.getPlaylistByStrategy(
				currentStrategy, getSelectionProfile(), config,
				chkIsCircular.isSelected(), true, null);
		showPlaylist(list, autoPlay);
	}

	private void showPlaylist(List<Song> list, boolean autoPlay) {
		PlaylistPreviewWindow win = PlaylistPreviewWindow.getInstance();
		win.init(list, autoPlay, getSelectionProfile());
	}

	protected void close() {
		writeSettings();
		playlistModel = new PlaylistProfileModel(playlistModel.getRowCount());
		table.setModel(playlistModel);
		this.setVisible(false);
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
		chooser.setFileFilter(new FileNameExtensionFilter("Partial Playlists",
				FileUtils.PARTIAL_PLAYLIST_EXT));
		int c = chooser.showOpenDialog(this);
		if (c != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = chooser.getSelectedFile();
		loadSelProfile(file);
	}

	public void loadSelProfile(File file) {
		SongSelectionProfile selProfile = FileUtils.loadSelectionProfile(file);
		setSelectedStrategy(selProfile.getStrategy());
		spnSize.setValue(selProfile.getSize());
		cmbSizeType.setSelectedIndex(selProfile.getSizeType());
		currentStrategySettings = selProfile.getStrategyConfig();
		chkIsCircular.setSelected(selProfile.isCircular());
		playlistModel.loadFromSelProfile(selProfile);
	}

	private void setSelectedStrategy(PlaylistGenerator strategy) {
		cmbStrategy.setSelectedItem(strategy);
	}

	/**
	 * New Selection profile
	 */
	protected void newSelectionProfile() {
		playlistModel = new PlaylistProfileModel(DEF_PLAYLIST_LEN);
		table.setModel(playlistModel);
	}

	public SongSelectionProfile getSelectionProfile() {
		SongSelectionProfile profile = playlistModel.getSelProfile();
		profile.setStrategy(currentStrategy);
		profile.setStrategyConfig(currentStrategySettings);
		profile.setIsCircular(chkIsCircular.isSelected());
		profile.setSizeType(cmbSizeType.getSelectedIndex());
		return profile;
	}

	/**
	 * Save the selection profile
	 * 
	 * @return success
	 */
	protected boolean saveSelProfile() {
		// TODO Auto-generated method stub
		SongSelectionProfile selProfile = getSelectionProfile();
		if (selProfile.getFile() == null) {
			// no file set. SaveAs...
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new FileNameExtensionFilter(
					"Partial Playlists", FileUtils.PARTIAL_PLAYLIST_EXT));
			int c = chooser.showSaveDialog(this);
			if (c != JFileChooser.APPROVE_OPTION) {
				return false;
			}
			File file = chooser.getSelectedFile();
			String fileName = file.toString();
			if (!fileName.endsWith("." + FileUtils.PARTIAL_PLAYLIST_EXT))
				fileName += "." + FileUtils.PARTIAL_PLAYLIST_EXT;
			try {
				FileUtils.writeXml(new File(fileName), selProfile.getXml());
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

	@Override
	public void setCurrentProfileSettings(StrategyConfiguration strategySettings) {
		this.currentStrategySettings = strategySettings;
		StrategyFactory.updateLastSettings(currentStrategy, strategySettings);
	}

}
