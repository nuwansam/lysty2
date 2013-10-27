package org.lysty.extractors;

import java.io.IOException;
import java.util.List;

import net.xeoh.plugins.base.Plugin;

import org.lysty.dao.Song;
import org.lysty.exceptions.FeatureExtractionException;

public interface FeatureExtractor extends Plugin {

	/**
	 * Extracts features and places them in the song.attributes
	 * 
	 * @param song
	 * @return
	 * @throws IOException
	 */
	public Song extract(Song song) throws FeatureExtractionException;

	public List<String> getSupportedAttributes();

	public List<String> getSupportedFileFormats();

}
