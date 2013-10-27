package org.lysty.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.lysty.core.PlaylistGenerator;
import org.lysty.dao.Song;
import org.lysty.dao.SongSelectionProfile;
import org.lysty.db.DBHandler;

public abstract class AbstractVoteMatchStrategy implements PlaylistGenerator {

	protected Map<String, String> attributes;

	@Override
	public List<Song> getPlaylist(SongSelectionProfile profile,
			StrategyConfiguration config) {
		attributes = config.getAttributes();
		readAttributes(attributes);
		Map<Song, Map<Song, Integer>> votesMap = getVotesMap(profile
				.getRelPosMap());

		List<Song> playlist = profile.getPartialPlaylist();

		Song prevSong = null;
		Song nextSong = null;
		Song cSong;
		int distF = playlist.size();
		int distB = 0;
		for (int i = 0; i < playlist.size(); i++) {
			cSong = playlist.get(i);
			if (cSong != null) {
				nextSong = cSong;
				distF = i;
				break;
			}
		}

		int totalVotes;
		int rand;
		Song selSong;
		Map<Song, Integer> candidates = new HashMap<Song, Integer>();
		Iterator<Entry<Song, Integer>> it;
		Entry<Song, Integer> entry;
		List<Song> alreadyIns = new ArrayList<Song>();
		for (int i = 0; i < playlist.size(); i++) {
			distB++;
			if (distF > 0)
				distF--;
			cSong = playlist.get(i);
			if (cSong != null) {
				prevSong = playlist.get(i);
				distB = 0;
				distF = 0;
				nextSong = null;
				for (int j = i + 1; j < playlist.size(); j++) {
					if (playlist.get(j) != null) {
						nextSong = playlist.get(j);
						distF = j - i;
						break;
					}
				}
				continue;
			}
			if (cSong == null) {
				// has to fill with a song for this position
				candidates = new HashMap<Song, Integer>();
				if (prevSong != null) {
					fillCandidates(candidates, votesMap.get(prevSong), distB
							/ (distB + distF), alreadyIns);
				}
				if (nextSong != null) {
					fillCandidates(candidates, votesMap.get(nextSong), distF
							/ (distB + distF), alreadyIns);
				}

				totalVotes = 0;
				it = candidates.entrySet().iterator();
				while (it.hasNext()) {
					entry = it.next();
					totalVotes += entry.getValue();
				}
				rand = (int) (Math.random() * totalVotes);
				selSong = getCandidateSong(candidates, rand);
				alreadyIns.add(selSong);
				playlist.set(i, selSong);
			}
		}
		return playlist;
	}

	protected abstract void readAttributes(Map<String, String> attributes);

	private void fillCandidates(Map<Song, Integer> candidates,
			Map<Song, Integer> votedSongs, float weight, List<Song> alreadyIns) {
		Iterator<Entry<Song, Integer>> it = votedSongs.entrySet().iterator();
		Entry<Song, Integer> entry;
		Integer cVotes;
		while (it.hasNext()) {
			entry = it.next();
			if (alreadyIns.contains(entry.getKey()))
				continue;
			cVotes = candidates.get(entry.getKey());
			cVotes = cVotes == null ? 0 : cVotes;
			cVotes += (int) Math.ceil(entry.getValue() * weight);
			candidates.put(entry.getKey(), cVotes);
		}
	}

	private Map<Song, Map<Song, Integer>> getVotesMap(
			Map<Song, Integer> relPosMap) {
		Song song;
		List<Song> allSongs = DBHandler.getInstance().getSongs(null);

		Iterator<Song> it = relPosMap.keySet().iterator();
		while (it.hasNext()) {
			song = it.next();
			allSongs.remove(song);
		}

		Map<Song, Map<Song, Integer>> votesMap = new HashMap<Song, Map<Song, Integer>>();
		Map<Song, Integer> listOfVotes;
		it = relPosMap.keySet().iterator();
		int votes;
		while (it.hasNext()) {
			song = it.next();
			listOfVotes = new HashMap<Song, Integer>();
			for (Song c : allSongs) {
				votes = getVotes(song, c);
				if (votes > 0) {
					listOfVotes.put(c, votes);
				}
			}
			votesMap.put(song, listOfVotes);
		}
		return votesMap;
	}

	protected abstract int getVotes(Song song, Song candidate);

	private Song getCandidateSong(Map<Song, Integer> candidates, int rand) {
		int cnt = 0;
		Iterator<Entry<Song, Integer>> it = candidates.entrySet().iterator();
		Entry<Song, Integer> entry;
		while (it.hasNext()) {
			entry = it.next();
			cnt += entry.getValue();
			if (cnt >= rand)
				return entry.getKey();
		}
		return null;
	}
}
