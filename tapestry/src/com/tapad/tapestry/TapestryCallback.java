package com.tapad.tapestry;

/**
 * A callback that handles a {@link TapestryResponse} asynchronously. Invoked by {@link TapestryClient}.
 */
public interface TapestryCallback {
	/**
	 * Receives the Tapestry response.
	 * 
	 * @param response
	 *            The response from Tapestry
	 * @param exception
	 *            Any exception thrown if the request fails, otherwise null
	 * @param millisSinceInvocation
	 *            Time since the request was sent. If a request is sent and the user has no network connection, this
	 *            time will be as long as it takes for a network connection to be found.
	 */
	public void receive(TapestryResponse response, Exception exception, long millisSinceInvocation);

	public static TapestryCallback DO_NOTHING = new TapestryCallback() {
		@Override
		public void receive(TapestryResponse response, Exception e, long millisSinceInvocation) {
		}
	};
}