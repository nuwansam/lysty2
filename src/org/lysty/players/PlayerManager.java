package org.lysty.players;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

	public static PlayerManager self = null;
	private Map<String, AbstractPlayer> playerMap;

	public static PlayerManager getInstance() {
		if (self == null) {
			self = new PlayerManager();
		}
		return self;
	}

	private PlayerManager() {
		playerMap = new HashMap<String, AbstractPlayer>();
		loadPlayers();
	}

	private void loadPlayers() {
		playerMap.put("mp3", new Mp3Player());
	}

	public AbstractPlayer getPlayer(String format) {
		return playerMap.get(format);
	}

}
