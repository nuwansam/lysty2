package org.lysty.ui;

import java.util.ArrayList;
import java.util.List;

public class WindowManager {

	List<LFrame> frames;

	public static WindowManager self = null;

	public static WindowManager getInstance() {
		if (self == null) {
			self = new WindowManager();
		}
		return self;
	}

	private WindowManager() {
		frames = new ArrayList<LFrame>();
	}

	public void registerWindow(LFrame frame) {
		frames.add(frame);
	}

	public void unregisterWindow(LFrame frame) {
		frames.remove(frame);
		if (frames.isEmpty()) {
			System.exit(0);
		}
	}
}
