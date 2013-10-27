package org.lysty.strategies.metatagmatch;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.lysty.core.ExtractorManager;
import org.lysty.extractors.FeatureExtractor;
import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.StrategyConfiguration;

public class MetaTagMatchStrategySettingsFrame extends AbstractStrategySettingsPanel {

	JComboBox cmbFeatures;

	@Override
	public StrategyConfiguration getConfig() {
		StrategyConfiguration config = new StrategyConfiguration();
		config.setAttribute(MetaTagMatchStrategy.FEATURE_TO_MATCH, cmbFeatures
				.getSelectedItem().toString());
		return config;
	}

	@Override
	public void createUI() {
		JLabel lblFeature = new JLabel("Feature to match on");
		FeatureExtractor extractor = ExtractorManager
				.getExtractor("org.lysty.extractors.MetaTagExtractor");
		cmbFeatures = new JComboBox(extractor.getSupportedAttributes()
				.toArray());
		this.add(lblFeature);
		this.add(cmbFeatures);
		this.setSize(500, 100);
	}

	@Override
	public void setConfig(StrategyConfiguration config) {
		cmbFeatures.setSelectedItem(config.getAttributes()
				.get(MetaTagMatchStrategy.FEATURE_TO_MATCH).toString());
	}

	@Override
	public Dimension getPrefferedDimensions() {
		return this.getSize();
	}

}
