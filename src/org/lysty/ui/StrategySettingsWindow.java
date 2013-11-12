package org.lysty.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.lysty.core.PlaylistGenerator;
import org.lysty.core.StrategyFactory;
import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.StrategyConfiguration;

public class StrategySettingsWindow extends LFrame {

	private static final double CONTROL_PANEL_HEIGHT = 20;
	private static StrategySettingsWindow self = null;

	public static StrategySettingsWindow getInstance() {
		if (self == null) {
			self = new StrategySettingsWindow();
		}
		return self;
	}

	private StrategySettingsListener playlistProfileWindow;
	private AbstractStrategySettingsPanel settingsPanel;
	private PlaylistGenerator strategy;

	private StrategySettingsWindow() {
		super("Fill Method Settings");
		// TODO Auto-generated constructor stub
	}

	public void setListener(StrategySettingsListener playlistProfileWindow) {
		this.playlistProfileWindow = playlistProfileWindow;
	}

	public void showUi(Component parent) {
		double w = settingsPanel.getPreferredSize().getWidth();
		double h = settingsPanel.getPreferredSize().getHeight()
				+ CONTROL_PANEL_HEIGHT;
		this.setSize((int) w, (int) h);
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(parent);
	}

	public void createUi(PlaylistGenerator strategy, Component parent) {
		this.strategy = strategy;
		settingsPanel = StrategyFactory.getStrategySettingsPanel(strategy);
		JPanel contentPanel = new JPanel(new MigLayout("insets 6 6 6 6"));
		this.setContentPane(contentPanel);
		contentPanel.add(settingsPanel, "span");
		contentPanel.add(getControlPanel(), "span");
		showUi(parent);
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
