package org.lysty.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

public class PlayerPanel extends JPanel {

	private JProgressBar progress;
	private JButton btnStartPause;
	private JButton btnStop;
	private JButton btnNext;
	private JButton btnPrev;
	private PlayPanelListener listener;
	private JCheckBox chkInfin;
	private JButton btnSleep;
	private JButton btnRandom;

	public PlayerPanel(PlayPanelListener listener) {
		this.listener = listener;
		createUI();
		layoutControls();
	}

	private void layoutControls() {
		MigLayout layout = new MigLayout();
		this.setLayout(layout);
		this.add(progress, "span");
		this.add(btnStop);
		this.add(btnStartPause);
		this.add(btnPrev);
		this.add(btnNext, "wrap");
		this.add(chkInfin);
		this.add(btnRandom);
		this.add(btnSleep, "align right");
	}

	private void createUI() {
		progress = new JProgressBar();
		btnStartPause = new JButton(new AbstractAction("Start") {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (btnStartPause.getText().equals("Start")) {
					listener.play();
					btnStartPause.setText("Pause");
				} else if (btnStartPause.getText().equals("Pause")) {
					listener.pause();
					btnStartPause.setText("Start");
				}
			}
		});
		btnStop = new JButton(new AbstractAction("Stop") {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.stop();
			}
		});
		btnNext = new JButton(new AbstractAction("Next") {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.next();
			}
		});
		btnPrev = new JButton(new AbstractAction("Prev") {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.prev();
			}
		});

		chkInfin = new JCheckBox("Infini Play");
		chkInfin.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				listener.setInfinyPlay(chkInfin.isSelected());
			}
		});
		btnSleep = new JButton(new AbstractAction("Sleep") {

			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem mnuHour = new JMenuItem(new AbstractAction("1 Hour") {

					@Override
					public void actionPerformed(ActionEvent e) {
						listener.setTimer(60);
					}
				});
				popup.add(mnuHour);
				popup.setVisible(true);
			}
		});
		btnRandom = new JButton(new AbstractAction("Randomize") {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.setRandomize(true);
			}
		});

	}
}
