package com.tapad.tapestry;

/**
 * A callback that handles a {@link TapestryResponse} asynchronously.  Invoked by {@link TapestryClient}.
 */
public interface TapestryCallback {
    public void receive(TapestryResponse response);
}
