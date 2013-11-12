package org.lysty.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

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

	public static AppSettingsWindow getInstance() {
		if (self == null) {
			self = new AppSettingsWindow("");
		}
		return self;
	}

	private AppSettingsWindow(String title) {
		super("Settings");
		createUI();
		layoutUI();
	}

	private void layoutUI() {
		// TODO Auto-generated method stub
		JPanel contentPane = new JPanel();
		contentPane.add(tabs);
		setContentPane(contentPane);
		setSize(500, 500);
		pack();
	}

	private void createUI() {
		tabs = new JTabbedPane();
		JPanel pnlPlayback = new JPanel();

		chkPlayNextOnSongLoad = new JCheckBox(
				"Do not stop current playing song when another song file is clicked");
		pnlPlayback.add(chkPlayNextOnSongLoad);
		tabs.add("Playback", pnlPlayback);
	}

}
