package org.lysty.players;

import org.lysty.players.PlayEvent.EventType;

public class PlayEvent {

	public enum EventType {
		PLAY_FINISHED, PLAY_STARTED
	}

	EventType eventType;

	public PlayEvent(EventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the eventType
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * @param eventType
	 *            the eventType to set
	 */
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

}
