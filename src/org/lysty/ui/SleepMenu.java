package org.lysty.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

public class SleepMenu extends JPopupMenu {

	protected static final int MINS_TO_MILIS = 60000;

	public SleepMenu(final PlayPanelListener listener) {
		final JCheckBoxMenuItem mnu10Mins = new JCheckBoxMenuItem("10 Mins");
		mnu10Mins.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (mnu10Mins.isSelected()) {
					listener.setTimer(10 * MINS_TO_MILIS);
				}
			}

		});

		final JCheckBoxMenuItem mnu20Mins = new JCheckBoxMenuItem("20 Mins");
		mnu20Mins.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (mnu20Mins.isSelected())
					listener.setTimer(20 * MINS_TO_MILIS);
			}
		});
		final JCheckBoxMenuItem mnu30Mins = new JCheckBoxMenuItem("30 Mins");
		mnu30Mins.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (mnu30Mins.isSelected())
					listener.setTimer(30 * MINS_TO_MILIS);
			}
		});
		final JCheckBoxMenuItem mnu40Mins = new JCheckBoxMenuItem("40 Mins");
		mnu40Mins.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (mnu40Mins.isSelected())
					listener.setTimer(40 * MINS_TO_MILIS);
			}
		});
		final JCheckBoxMenuItem mnu60Mins = new JCheckBoxMenuItem("1 Hour");
		mnu60Mins.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (mnu60Mins.isSelected())
					listener.setTimer(60 * MINS_TO_MILIS);
				mnu60Mins.setSelected(true);
			}
		});

		JCheckBoxMenuItem mnuCancel = new JCheckBoxMenuItem(new AbstractAction(
				"No Sleep") {

			@Override
			public void actionPerformed(ActionEvent e) {
				listener.cancelTimer();
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(mnuCancel);
		group.add(mnu10Mins);
		group.add(mnu20Mins);
		group.add(mnu30Mins);
		group.add(mnu40Mins);
		group.add(mnu60Mins);

		add(mnuCancel);
		add(mnu10Mins);
		add(mnu20Mins);
		add(mnu30Mins);
		add(mnu40Mins);
		add(mnu60Mins);

	}
}
