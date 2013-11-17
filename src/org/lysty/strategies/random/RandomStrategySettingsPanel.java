package org.lysty.strategies.random;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.StrategyConfiguration;

public class RandomStrategySettingsPanel extends AbstractStrategySettingsPanel {
	private static final Object DEF_COMMON_FOLDER_HEIGHT = 2;
	JSpinner txtCommonFolderHeight;

	@Override
	public StrategyConfiguration getConfig() {
		StrategyConfiguration config = new StrategyConfiguration();
		config.setAttribute(RandomStrategy.COMMON_FOLDER_HEIGHT,
				txtCommonFolderHeight.getValue() + "");
		return config;
	}

	@Override
	public void createUI() {
		this.setLayout(new MigLayout());
		JLabel lblCommonFolderHeight = new JLabel("Common Folder Height");
		txtCommonFolderHeight = new JSpinner();
		add(lblCommonFolderHeight);
		add(txtCommonFolderHeight);
	}

	@Override
	public void setConfig(StrategyConfiguration config) {
		try {
			txtCommonFolderHeight.setValue(Integer.parseInt(config
					.getAttributes().get(RandomStrategy.COMMON_FOLDER_HEIGHT)));
		} catch (Exception e) {
			txtCommonFolderHeight.setValue(DEF_COMMON_FOLDER_HEIGHT);
		}

	}

	@Override
	public Dimension getPrefferedDimensions() {
		return new Dimension(60, 50);
	}

}
