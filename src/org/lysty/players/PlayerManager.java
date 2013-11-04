package org.lysty.players;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerManager {

	public static PlayerManager self = null;
	private Map<String, AbstractPlayer> playerMap;
	private Set<String> supportedFormats;

	public static PlayerManager getInstance() {
		if (self == null) {
			self = new PlayerManager();
		}
		return self;
	}

	private PlayerManager() {
		playerMap = new HashMap<String, AbstractPlayer>();
		supportedFormats = new HashSet<String>();
		loadPlayers();
	}

	private void loadPlayers() {
		AbstractPlayer player = new Mp3Player();
		List<String> supports = player.getSupportedFormats();
		for (String format : supports) {
			playerMap.put(format.toLowerCase(), player);
			supportedFormats.add(format.toLowerCase());
		}
	}

	public Set<String> getSupportedFormats() {
		return supportedFormats;
	}

	public AbstractPlayer getPlayer(String format) {
		return playerMap.get(format);
	}

}
