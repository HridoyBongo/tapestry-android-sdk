package com.tapad.tapestry;

import android.util.Log;

/**
 * Convenience class to log using com.tapad as log tag.
 */
public class Logging {
	private static final String TAG = "com.tapad";

	public static void d(String message) {
		Log.d(TAG, message);
	}

	public static void d(String message, Throwable caught) {
		Log.d(TAG, message, caught);
	}

	public static void w(String message) {
		Log.w(TAG, message);
	}

	public static void w(String message, Throwable caught) {
		Log.w(TAG, message, caught);
	}

	public static void e(String message) {
		Log.e(TAG, message);
	}

	public static void e(String message, Throwable caught) {
		Log.e(TAG, message, caught);
	}
}