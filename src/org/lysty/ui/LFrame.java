package org.lysty.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class LFrame extends JFrame {

	public LFrame(String title) {
		super(title);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				WindowManager.getInstance().unregisterWindow(LFrame.this);
			}
		});
	}
}
