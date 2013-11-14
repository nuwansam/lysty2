package org.lysty.players;


public class PlayEvent {

	public enum EventType {
		PLAY_STARTED, PLAY_EXCEPTION, SONG_ENDED, SONG_STOPPED, SONG_PAUSED
	}

	EventType eventType;
	private int frame;

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

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public int getFrame() {
		return this.frame;
	}

}
