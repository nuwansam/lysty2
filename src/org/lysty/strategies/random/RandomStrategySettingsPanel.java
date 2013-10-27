package org.lysty.strategies.random;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.StrategyConfiguration;

public class RandomStrategySettingsPanel extends AbstractStrategySettingsPanel {
	JTextField txtCommonFolderHeight;

	@Override
	public StrategyConfiguration getConfig() {
		StrategyConfiguration config = new StrategyConfiguration();
		config.setAttribute(RandomStrategy.COMMON_FOLDER_HEIGHT,
				txtCommonFolderHeight.getText().trim() + "");
		return config;
	}

	@Override
	public void createUI() {
		this.setLayout(new MigLayout());
		JLabel lblCommonFolderHeight = new JLabel("Common Folder Height");
		txtCommonFolderHeight = new JTextField();
		add(lblCommonFolderHeight);
		add(txtCommonFolderHeight);
	}

	@Override
	public void setConfig(StrategyConfiguration config) {
		txtCommonFolderHeight.setText(config.getAttributes().get(
				RandomStrategy.COMMON_FOLDER_HEIGHT));
	}

	@Override
	public Dimension getPrefferedDimensions() {
		return new Dimension(60, 50);
	}

}
