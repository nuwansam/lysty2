package org.lysty.strategies;

import java.awt.Dimension;

import org.lysty.ui.LPanel;

public abstract class AbstractStrategySettingsPanel extends LPanel {

	public abstract StrategyConfiguration getConfig();

	public abstract void createUI();

	public abstract void setConfig(StrategyConfiguration config);

	public abstract Dimension getPrefferedDimensions();

}
