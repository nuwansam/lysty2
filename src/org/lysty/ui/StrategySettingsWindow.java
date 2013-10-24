package org.lysty.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.lysty.core.PlaylistGenerator;
import org.lysty.core.StrategyFactory;
import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.StrategyConfiguration;

public class StrategySettingsWindow extends LFrame {

	private static StrategySettingsWindow self = null;

	public static StrategySettingsWindow getInstance() {
		if (self == null) {
			self = new StrategySettingsWindow();
		}
		return self;
	}

	private PlaylistProfileWindow playlistProfileWindow;
	private AbstractStrategySettingsPanel settingsPanel;
	private PlaylistGenerator strategy;

	private StrategySettingsWindow() {
		super("Fill Method Settings");
		// TODO Auto-generated constructor stub
	}

	public void setProfileWindow(PlaylistProfileWindow playlistProfileWindow) {
		this.playlistProfileWindow = playlistProfileWindow;
	}

	public void showUi() {
		this.setSize(500, 500);
		this.setVisible(true);
	}

	public void createUi(PlaylistGenerator strategy) {
		this.strategy = strategy;
		settingsPanel = StrategyFactory.getStrategySettingsPanel(strategy);
		JPanel contentPanel = new JPanel();
		this.setContentPane(contentPanel);
		contentPanel.add(settingsPanel);
		contentPanel.add(getControlPanel());
		showUi();
	}

	private JPanel getControlPanel() {
		JPanel panel = new JPanel();
		JButton btnRestor = new JButton(new AbstractAction(
				"Restore Defaults...") {

			@Override
			public void actionPerformed(ActionEvent e) {

				StrategySettingsWindow.getInstance().settingsPanel
						.setConfig(StrategyFactory.getDefaultSettings(strategy));

			}
		});

		JButton btnOk = new JButton(new AbstractAction("Ok") {

			@Override
			public void actionPerformed(ActionEvent e) {
				StrategySettingsWindow.getInstance().playlistProfileWindow
						.setCurrentProfileSettings(StrategySettingsWindow
								.getInstance().getStrategySettngs());
				StrategySettingsWindow.getInstance().setVisible(false);
			}
		});

		JButton btnCancel = new JButton(new AbstractAction("Cancel") {

			@Override
			public void actionPerformed(ActionEvent e) {
				StrategySettingsWindow.getInstance().setVisible(false);
			}
		});

		panel.add(btnRestor);
		panel.add(btnOk);
		panel.add(btnCancel);
		return panel;
	}

	protected StrategyConfiguration getStrategySettngs() {
		return settingsPanel.getConfig();
	}
}
