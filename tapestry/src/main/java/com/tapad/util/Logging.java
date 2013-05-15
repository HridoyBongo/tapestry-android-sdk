package com.tapad.util;

import android.util.Log;

public class Logging {
    public static boolean enabled = false;
    public static boolean throwExceptions = false;

    public static void debug(String tag, String message) {
        if (enabled) Log.d(tag, message);
    }

    public static void warn(String tag, String message) {
        if (enabled) Log.w(tag, message);
    }

    public static void error(String tag, String message, Exception e) {
        if (enabled) Log.e(tag, message);
        if (throwExceptions) throw new RuntimeException(e);
    }
}
