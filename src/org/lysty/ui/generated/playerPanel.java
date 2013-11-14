package org.lysty.ui.generated;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class playerPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public playerPanel() {
		setLayout(new MigLayout("", "[grow]", "[][grow]"));

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		add(panel, "cell 0 0,grow");
		panel.setLayout(new MigLayout("", "[grow]", "[][]"));

		JProgressBar progressBar = new JProgressBar();
		panel.add(progressBar, "cell 0 0,alignx center");

		JButton btnStartPause = new JButton("");
		btnStartPause.setIcon(new ImageIcon(playerPanel.class
				.getResource("/icons/play.png")));
		panel.add(btnStartPause, "flowx,cell 0 1");

		JButton btnStop = new JButton("New button");
		panel.add(btnStop, "cell 0 1,aligny bottom");

		JButton btnPrev = new JButton("New button");
		panel.add(btnPrev, "cell 0 1");

		JButton btnNext = new JButton("New button");
		panel.add(btnNext, "cell 0 1");

		JButton btnRandom = new JButton("New button");
		panel.add(btnRandom, "cell 0 1,push ,alignx right");

		JButton btnSleep = new JButton("New button");
		panel.add(btnSleep, "cell 0 1,alignx right");

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		add(panel_1, "cell 0 1,grow");
		panel_1.setLayout(new MigLayout("", "[][grow][]", "[]"));

		JButton btnInfin = new JButton("New button");
		panel_1.add(btnInfin, "cell 0 0,aligny top");

		JComboBox cmbFills = new JComboBox();
		panel_1.add(cmbFills, "cell 1 0,growx");

		JButton btnFillSettings = new JButton("New button");
		panel_1.add(btnFillSettings, "cell 2 0");

	}

}
