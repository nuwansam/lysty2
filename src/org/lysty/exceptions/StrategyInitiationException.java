package org.lysty.exceptions;

public class StrategyInitiationException extends Exception {

	String message;

	public StrategyInitiationException(String message) {
		this.message = message;
	}

	public static final String NOT_A_PLAYLIST_GENERATOR = "Not a playlist generator";

}
