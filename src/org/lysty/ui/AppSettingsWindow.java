package org.lysty.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.lysty.core.AppSettingsManager;
import org.lysty.util.Utils;

/**
 * The application level settings window
 * 
 * @author NuwanSam
 * 
 */
public class AppSettingsWindow extends LFrame {

	private static AppSettingsWindow self = null;

	JTabbedPane tabs;
	JCheckBox chkPlayNextOnSongLoad;

	private JButton btnOk;

	private boolean playNextWhenLoadOnCurrentPlay;

	private JButton btnCancel;

	private JPanel controlPanel;

	private JCheckBox chkRemAutoGensOnManualAdd;

	protected boolean remAutoGensOnManualAdd;

	private JTextField txtInfiniPlayLastNtoCheck;

	private JTextField txtGenListLength;

	public static AppSettingsWindow getInstance() {
		if (self == null) {
			self = new AppSettingsWindow("");
		}
		return self;
	}

	private AppSettingsWindow(String title) {
		super("Settings");
	}

	public void init(Component parent) {
		createUI();
		layoutUI();
		loadValues();
		setVisible(true);
		setLocationRelativeTo(parent);
	}

	private void loadValues() {
		// playback settings
		playNextWhenLoadOnCurrentPlay = AppSettingsManager
				.getPropertyAsBoolean(AppSettingsManager.PLAY_NEXT_WHEN_LOAD_ON_CURRENT_PLAY);
		chkPlayNextOnSongLoad.setSelected(playNextWhenLoadOnCurrentPlay);
		remAutoGensOnManualAdd = AppSettingsManager
				.getPropertyAsBoolean(AppSettingsManager.REM_UNPLAYED_AUTOGENS_ON_MANUAL_ADD);
		chkRemAutoGensOnManualAdd.setSelected(remAutoGensOnManualAdd);

		// infini play settings
		txtGenListLength.setText(AppSettingsManager
				.getProperty(AppSettingsManager.INFINIPLAY_GENLIST_SIZE));
		txtInfiniPlayLastNtoCheck.setText(AppSettingsManager
				.getProperty(AppSettingsManager.INFINIPLAY_LAST_N_TO_CHECK));
	}

	private void layoutUI() {
		// TODO Auto-generated method stub
		JPanel contentPane = new JPanel(new MigLayout());
		contentPane.add(tabs, "span");
		contentPane.add(controlPanel);
		setContentPane(contentPane);
		setSize(500, 500);
		pack();
	}

	private void createUI() {
		tabs = new JTabbedPane();
		createPlaybackTab();
		createInfiniPlayTab();
		createControlPanel();
	}

	private void createInfiniPlayTab() {
		JPanel panel = new JPanel(new MigLayout());

		txtInfiniPlayLastNtoCheck = new JTextField();
		JLabel lblLastN = new JLabel("Consider the Last ");
		JLabel lblLastN2 = new JLabel(" songs played for Infini play");
		JPanel pnlLastN = new JPanel();
		pnlLastN.add(lblLastN);
		pnlLastN.add(txtInfiniPlayLastNtoCheck);
		pnlLastN.add(lblLastN2);

		txtGenListLength = new JTextField();
		JLabel lblGenLength = new JLabel("Generate list length");
		JPanel pnlGenLength = new JPanel();
		pnlGenLength.add(lblGenLength);
		pnlGenLength.add(txtGenListLength);

		panel.add(pnlLastN, "wrap");
		panel.add(pnlGenLength);
		tabs.add("Infini Play", panel);
	}

	private void createControlPanel() {
		btnOk = new JButton(new AbstractAction("Ok") {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (validateSettings()) {
					AppSettingsManager.updateAppSettings(getSettings());
					AppSettingsWindow.this.setVisible(false);
				} else {
					JOptionPane.showMessageDialog(AppSettingsWindow.this,
							"Incorrect settings");
				}

			}
		});
		btnCancel = new JButton(new AbstractAction("Cancel") {

			@Override
			public void actionPerformed(ActionEvent e) {
				AppSettingsWindow.this.setVisible(false);
			}
		});
		controlPanel = new JPanel(new MigLayout());
		controlPanel.add(btnOk);
		controlPanel.add(btnCancel);
	}

	protected boolean validateSettings() {
		String str = txtGenListLength.getText();
		if (!Utils.isNumber(str)) {
			return false;
		}
		str = txtInfiniPlayLastNtoCheck.getText();
		if (!Utils.isNumber(str)) {
			return false;
		}
		return true;
	}

	protected Map<String, String> getSettings() {
		Map<String, String> map = new HashMap<String, String>();
		map.put(AppSettingsManager.PLAY_NEXT_WHEN_LOAD_ON_CURRENT_PLAY,
				playNextWhenLoadOnCurrentPlay ? "true" : "false");
		map.put(AppSettingsManager.REM_UNPLAYED_AUTOGENS_ON_MANUAL_ADD,
				remAutoGensOnManualAdd ? "true" : "false");
		map.put(AppSettingsManager.INFINIPLAY_GENLIST_SIZE,
				txtGenListLength.getText());
		map.put(AppSettingsManager.INFINIPLAY_LAST_N_TO_CHECK,
				txtInfiniPlayLastNtoCheck.getText());
		return map;
	}

	private void createPlaybackTab() {
		JPanel pnlPlayback = new JPanel(new MigLayout());

		chkPlayNextOnSongLoad = new JCheckBox(
				new AbstractAction(
						"Do not stop current playing song when another song file is clicked") {

					@Override
					public void actionPerformed(ActionEvent e) {
						playNextWhenLoadOnCurrentPlay = chkPlayNextOnSongLoad
								.isSelected();
					}
				});

		chkRemAutoGensOnManualAdd = new JCheckBox(
				new AbstractAction(
						"Remove unplayed autogenerated songs when a song is manually added to the playlist") {

					@Override
					public void actionPerformed(ActionEvent e) {
						remAutoGensOnManualAdd = chkRemAutoGensOnManualAdd
								.isSelected();
					}
				});

		pnlPlayback.add(chkPlayNextOnSongLoad, "wrap");
		pnlPlayback.add(chkRemAutoGensOnManualAdd);
		tabs.add("Playback", pnlPlayback);
	}

}
