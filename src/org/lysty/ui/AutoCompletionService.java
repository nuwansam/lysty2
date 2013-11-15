package org.lysty.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.lysty.dao.Song;

import sas.samples.CompletionService;

/**
 * Auto complete service. maintains a map of fields vs current values for each
 * of those fields.
 * 
 * @author NuwanSam
 * 
 */
public class AutoCompletionService implements CompletionService<String> {

	private String currentField;
	private Map<String, TreeSet<String>> map;

	public void setValueMap(Map<String, TreeSet<String>> valueMap) {
		this.map = valueMap;
	}

	public void addValueToMap(String field, String value) {
		TreeSet<String> set = map.get(field);
		if (set == null) {
			set = new TreeSet<String>();
		}
		set.add(value);
		map.put(field, set);
	}

	public void setCurrentField(String cField) {
		this.currentField = cField;
	}

	@Override
	public String autoComplete(String str) {
		str = str.toLowerCase();
		if (str.length() < 4)
			return null;
		TreeSet<String> set = map.get(currentField);
		if (set == null)
			return null;
		String suggest = set.ceiling(str);
		if (suggest.startsWith(str))
			return suggest;
		return null;
	}

	public void createMapFromSongList(List<Song> allSongs) {
		map = new HashMap<String, TreeSet<String>>();
		Map<String, String> attribMap;
		Iterator<Entry<String, String>> it;
		Entry<String, String> entry;
		TreeSet<String> valueSet;

		for (Song song : allSongs) {
			attribMap = song.getAttributes();
			it = attribMap.entrySet().iterator();
			while (it.hasNext()) {
				entry = it.next();
				valueSet = map.get(entry.getKey());
				if (valueSet == null) {
					valueSet = new TreeSet<String>();
				}
				valueSet.add(entry.getValue().trim().toLowerCase());
				map.put(entry.getKey(), valueSet);
			}
		}
	}

}
