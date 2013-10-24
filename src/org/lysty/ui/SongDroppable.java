package org.lysty.ui;

import java.io.File;

import org.lysty.ui.exception.SongNotIndexedException;

public interface SongDroppable {

	public void addSong(File file, int position) throws SongNotIndexedException;
}
