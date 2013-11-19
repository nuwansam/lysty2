package org.lysty.ui;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.lysty.util.Utils;

public class LFrame extends JFrame {

	public LFrame(String title) {
		super(title);
		this.setIconImage(((ImageIcon) Utils
				.getIcon(ResourceConstants.LYSTY_ICON)).getImage());
	}
}
