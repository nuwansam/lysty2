package org.lysty.extractors;

import java.util.List;
import java.util.Map;

import org.lysty.core.ExtractorManager;
import org.lysty.dao.Song;
import org.lysty.db.DBHandler;
import org.lysty.exceptions.FeatureExtractionException;

public class ExtractSequencer {

	private static ExtractSequencer self = null;
	private List<FeatureExtractor> extractors;

	private ExtractSequencer() {
		extractors = ExtractorManager.getExtractors();
	}

	public static ExtractSequencer getInstance() {
		if (self == null) {
			self = new ExtractSequencer();
		}
		return self;
	}

	public void extract(Song song, Long lastIndexedTime)
			throws FeatureExtractionException {
		if (lastIndexedTime == null)
			lastIndexedTime = 0l;
		Long timestamp;
		for (FeatureExtractor extractor : extractors) {
			timestamp = ExtractorManager.getExtractorTimestamp(extractor);
			if (timestamp > lastIndexedTime) {
				extractor.extract(song);
			}
		}
	}
}
