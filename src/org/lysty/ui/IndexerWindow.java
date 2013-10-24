package org.lysty.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.lysty.core.SongIndexer;
import org.lysty.core.UpdateListener;

public class IndexerWindow extends LFrame {

	private static final String CAPTION_ADD_FOLDER = "Add Folder";
	private static final String CAPTION_INDEX_DEPTH = "Index Depth";
	private static final String CAPTION_IS_INCREMENTAL = "Is Incremental";
	private static final String BTN_CAPTION_INDEX = "Index...";
	/**
	 * 
	 */
	private static final long serialVersionUID = -44396160001605308L;
	private static final int INDEXER_WIDTH = 400;
	private static final int INDEXER_HEIGHT = 300;
	private static IndexerWindow self;
	private DefaultListModel<File> model;

	JList lstFolders;
	JCheckBox chkIsIncremental;
	JTextField txtDepth;
	JButton btnIndex;

	private JButton btnAddFolder;
	private JScrollPane scroller;

	private IndexerWindow(String title) {
		super(BTN_CAPTION_INDEX);
		createControls();
		layoutUI();
	}

	private void layoutUI() {
		JPanel panel = new JPanel();
		MigLayout layout = new MigLayout("insets 6 6 6 6");
		panel.setLayout(layout);

		panel.add(btnAddFolder);
		JLabel lblDepth = new JLabel("Depth");
		panel.add(lblDepth);
		panel.add(txtDepth);
		panel.add(chkIsIncremental);
		panel.add(btnIndex, "wrap");
		panel.add(scroller, "span,growx");
		this.setContentPane(panel);
		this.pack();
	}

	private void createControls() {
		btnAddFolder = new JButton(new AbstractAction(CAPTION_ADD_FOLDER) {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(true);
				int r = chooser.showOpenDialog(IndexerWindow.this);
				if (r != JFileChooser.CANCEL_OPTION) {
					File[] files = chooser.getSelectedFiles();
					for (File file : files) {
						model.addElement(file);
					}
				}
			}
		});

		btnIndex = new JButton(new AbstractAction(BTN_CAPTION_INDEX) {

			@Override
			public void actionPerformed(ActionEvent e) {
				index();
			}
		});
		lstFolders = new JList();

		chkIsIncremental = new JCheckBox(CAPTION_IS_INCREMENTAL);
		txtDepth = new JTextField();

		setDefaults();
		model = new DefaultListModel<File>();
		lstFolders.setModel(model);
		scroller = new JScrollPane(lstFolders);

		this.setVisible(true);
		this.setSize(INDEXER_WIDTH, INDEXER_HEIGHT);
	}

	private void setDefaults() {
		txtDepth.setText(SongIndexer.DEFAULT_DEPTHS_TO_INDEX + "");
		chkIsIncremental.setSelected(true);
	}

	public static IndexerWindow getInstance() {
		if (self == null) {
			self = new IndexerWindow("");
		}
		return self;
	}

	public void setFiles(File[] files) {
		for (File file : files) {
			model.addElement(file);
		}
	}

	public void index() {
		final File[] files = new File[model.size()];
		for (int i = 0; i < model.size(); i++) {
			files[i] = model.get(i);
		}

		Thread thread = new Thread() {

			@Override
			public void run() {
				IndexProgressWindow indexProgressWindow = new IndexProgressWindow();
				indexProgressWindow.setLocationRelativeTo(IndexerWindow.this);
				SongIndexer.index(files, Integer.parseInt(txtDepth.getText()),
						chkIsIncremental.isSelected(), indexProgressWindow);
			}
		};
		thread.start();
	}

}
