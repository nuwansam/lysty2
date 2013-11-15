package org.lysty.ui;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;

import org.apache.log4j.Logger;

public final class VolumeControl {

	public static Logger logger = Logger.getLogger(VolumeControl.class);

	public static void setVolume(float f) {
		javax.sound.sampled.Port.Info source = Port.Info.SPEAKER;
		// source = Port.Info.LINE_OUT;
		// source = Port.Info.HEADPHONE;

		if (AudioSystem.isLineSupported(source)) {
			try {
				Port outline = (Port) AudioSystem.getLine(source);
				outline.open();
				FloatControl volumeControl = (FloatControl) outline
						.getControl(FloatControl.Type.VOLUME);
				volumeControl.setValue(f);
			} catch (LineUnavailableException ex) {
				logger.error("source not supported", ex);
				ex.printStackTrace();
			}
		}
	}

}