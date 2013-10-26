package org.lysty.ui;

public interface PlayPanelListener {

	public void play();

	public void pause();

	public void stop();

	public void next();

	public void prev();

	public void setInfinyPlay(boolean isInfini);

	public void setTimer(int time);

	public void setRandomize(boolean isRandom);
}
