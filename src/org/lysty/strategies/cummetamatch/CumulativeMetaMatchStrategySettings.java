package org.lysty.strategies.cummetamatch;

import java.awt.Dimension;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;

import org.lysty.strategies.AbstractStrategySettingsPanel;
import org.lysty.strategies.StrategyConfiguration;
import org.lysty.util.Utils;

public class CumulativeMetaMatchStrategySettings extends
		AbstractStrategySettingsPanel {

	private JSpinner spnAlbumW;
	private JSpinner spnReleaseW;
	private JSpinner spnArtistW;
	private JSpinner spnGenreW;
	private JSpinner spnComposerW;
	private JSpinner spnLangW;
	private JSpinner spnMoodW;
	private JSpinner spnFolderW;
	private JSpinner spnCommonFolderH;

	private int DEF_ALBUMW = 30;
	private int DEF_RELW = 5;
	private int DEF_ARTISTW = 20;
	private int DEF_GENREW = 8;
	private int DEF_COMPOSERW = 10;
	private int DEF_LANGW = 10;
	private int DEF_MOODW = 10;
	private int DEF_FOLDERW = 10;
	private int DEF_COMMONFOLDERW = 2;

	@Override
	public StrategyConfiguration getConfig() {
		StrategyConfiguration config = new StrategyConfiguration();
		config.setAttribute(CumulativeMetaMatchStrategy.ALBUM_VOTE_WEIGHT,
				spnAlbumW.getValue() + "");
		config.setAttribute(
				CumulativeMetaMatchStrategy.RELEASE_DATE_VOTE_WEIGHT,
				spnReleaseW.getValue() + "");
		config.setAttribute(CumulativeMetaMatchStrategy.ARTIST_VOTE_WEIGHT,
				spnArtistW.getValue() + "");
		config.setAttribute(CumulativeMetaMatchStrategy.GENRE_VOTE_WEIGHT,
				spnGenreW.getValue() + "");
		config.setAttribute(CumulativeMetaMatchStrategy.COMPOSER_VOTE_WEIGHT,
				spnComposerW.getValue() + "");
		config.setAttribute(CumulativeMetaMatchStrategy.LANGUAGE_VOTE_WEIGHT,
				spnLangW.getValue() + "");
		config.setAttribute(CumulativeMetaMatchStrategy.MOOD_VOTE_WEIGHT,
				spnMoodW.getValue() + "");
		config.setAttribute(CumulativeMetaMatchStrategy.FOLDER_VOTE_WEIGHT,
				spnFolderW.getValue() + "");
		config.setAttribute(CumulativeMetaMatchStrategy.COMMON_FOLDER_HEIGHT,
				spnCommonFolderH.getValue() + "");
		return config;
	}

	@Override
	public void createUI() {
		setLayout(new MigLayout("insets 2 2 2 2", "[][]", "[][][][][][][][][]"));
		JLabel lblAlbumW = new JLabel("Album Weight");
		JLabel lblReleaseDateW = new JLabel("Release Year Weight");
		JLabel lblArtistW = new JLabel("Artist Weight");
		JLabel lblGenreW = new JLabel("Genre Weight");
		JLabel lblComposerW = new JLabel("Composer Weight");
		JLabel lblLangW = new JLabel("Language Weight");
		JLabel lblMoodW = new JLabel("Mood Weight");
		JLabel lblFolderW = new JLabel("Folder Distance Weight");
		JLabel lblCommonFolderH = new JLabel("Common folder height");

		spnAlbumW = new JSpinner();
		spnReleaseW = new JSpinner();
		spnArtistW = new JSpinner();
		spnGenreW = new JSpinner();
		spnComposerW = new JSpinner();
		spnLangW = new JSpinner();
		spnMoodW = new JSpinner();
		spnFolderW = new JSpinner();
		spnCommonFolderH = new JSpinner();

		add(lblCommonFolderH, "sg lbl");
		add(spnCommonFolderH, "sg spn,wrap");
		add(lblAlbumW, "sg lbl");
		add(spnAlbumW, "sg spn,wrap");
		add(lblReleaseDateW, "sg lbl");
		add(spnReleaseW, "sg spn,wrap");
		add(lblArtistW, "sg lbl");
		add(spnArtistW, "sg spn,wrap");
		add(lblGenreW, "sg lbl");
		add(spnGenreW, "sg spn,wrap");
		add(lblComposerW, "sg lbl");
		add(spnComposerW, "sg spn,wrap");
		add(lblLangW, "sg lbl");
		add(spnLangW, "sg spn,wrap");
		add(lblMoodW, "sg lbl");
		add(spnMoodW, "sg spn,wrap");
		add(lblFolderW, "sg lbl");
		add(spnFolderW, "sg spn,wrap");
	}

	@Override
	public void setConfig(StrategyConfiguration config) {
		spnAlbumW.setValue(DEF_ALBUMW);
		spnReleaseW.setValue(DEF_RELW);
		spnArtistW.setValue(DEF_ARTISTW);
		spnGenreW.setValue(DEF_GENREW);
		spnComposerW.setValue(DEF_COMPOSERW);
		spnLangW.setValue(DEF_LANGW);
		spnMoodW.setValue(DEF_MOODW);
		spnCommonFolderH.setValue(DEF_COMMONFOLDERW);
		spnFolderW.setValue(DEF_FOLDERW);

		Map<String, String> attribMap = config.getAttributes();
		String attrStr = attribMap
				.get(CumulativeMetaMatchStrategy.ALBUM_VOTE_WEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnAlbumW.setValue(Integer.parseInt(attrStr));
		}
		attrStr = attribMap
				.get(CumulativeMetaMatchStrategy.RELEASE_DATE_VOTE_WEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnReleaseW.setValue(Integer.parseInt(attrStr));
		}
		attrStr = attribMap.get(CumulativeMetaMatchStrategy.ARTIST_VOTE_WEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnArtistW.setValue(Integer.parseInt(attrStr));
		}
		attrStr = attribMap.get(CumulativeMetaMatchStrategy.GENRE_VOTE_WEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnGenreW.setValue(Integer.parseInt(attrStr));
		}
		attrStr = attribMap
				.get(CumulativeMetaMatchStrategy.COMPOSER_VOTE_WEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnComposerW.setValue(Integer.parseInt(attrStr));
		}
		attrStr = attribMap
				.get(CumulativeMetaMatchStrategy.LANGUAGE_VOTE_WEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnLangW.setValue(Integer.parseInt(attrStr));
		}
		attrStr = attribMap.get(CumulativeMetaMatchStrategy.MOOD_VOTE_WEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnMoodW.setValue(Integer.parseInt(attrStr));
		}
		attrStr = attribMap
				.get(CumulativeMetaMatchStrategy.COMMON_FOLDER_HEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnCommonFolderH.setValue(Integer.parseInt(attrStr));
		}
		attrStr = attribMap.get(CumulativeMetaMatchStrategy.FOLDER_VOTE_WEIGHT);
		if (Utils.isNumber(attrStr)) {
			spnFolderW.setValue(Integer.parseInt(attrStr));
		}

	}

	@Override
	public Dimension getPrefferedDimensions() {
		return new Dimension(100, 600);
	}

}
