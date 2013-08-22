package com.tapad.tapestry;

import android.app.Activity;

/**
 * A convenience class for running callbacks in the Android UI thread.
 * <p/>
 * In situations where the UI will be updated based on the {@link TapestryResponse}, the callback will need to execute
 * it's view-updating methods on the UI thread. This class provides an convenient way to do so.
 */
public abstract class TapestryUICallback implements TapestryCallback {
	private final Activity activity;

	/**
	 * @param activity
	 *            An activity is required in order to access the {@code runOnUiThread} method
	 */
	public TapestryUICallback(Activity activity) {
		this.activity = activity;
	}

	public void receive(final TapestryResponse response, final Exception exception, final long millisSinceInvocation) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				receiveOnUiThread(response, exception, millisSinceInvocation);
			}
		});
	}

	/**
	 * Code that updates the UI should be implemented in this method
	 * 
	 * @param response
	 */
	public abstract void receiveOnUiThread(TapestryResponse response, Exception exception, long millisSinceInvocation);
}
