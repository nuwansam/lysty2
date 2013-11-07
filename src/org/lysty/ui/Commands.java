package org.lysty.ui;

public class Commands {

	public static void showIndexDialog(LFrame parent) {
		IndexerWindow.getInstance().setVisible(true);
		IndexerWindow.getInstance().setLocationRelativeTo(parent);
	}

}
