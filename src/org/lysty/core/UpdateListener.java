package org.lysty.core;

public interface UpdateListener {

	/**
	 * Set the size of the update - what is 100%
	 * 
	 * @param size
	 */
	public void setSize(long size);

	/**
	 * notify the current progress
	 * 
	 * @param currentProgress
	 */
	public void notifyUpdate(long currentProgress, String customMessage);

	public void notifyError(Exception e);

	public void notifyCurrentSuccessCount(long successCount);

	public void notifyComplete();

}
