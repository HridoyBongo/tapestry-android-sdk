package com.tapad.util;

import android.util.Log;

public class Logging {
    private static boolean enabled = false;
    private static boolean throwExceptions = false;

    public static void setThrowExceptions(boolean throwExceptions) {
        Logging.throwExceptions = throwExceptions;
    }

    public static void setEnabled(boolean enabled) {
        Logging.enabled = enabled;
    }

    public static void debug(Class<?> clazz, String message) {
        tryLog(clazz, "DEBUG", message);
    }

    public static void warn(Class<?> clazz, String message) {
        tryLog(clazz, "WARN", message);
    }

    public static void error(Class<?> clazz, String message, Exception e) {
        tryLog(clazz, "ERROR", message + ": " + e);
        if (throwExceptions) throw new RuntimeException(e);
    }

    private static void tryLog(Class<?> clazz, String logger, String message) {
        if (enabled)
            try {
                if (logger.equals("DEBUG")) Log.d(clazz.getCanonicalName(), message);
                if (logger.equals("WARN")) Log.w(clazz.getCanonicalName(), message);
                if (logger.equals("ERROR")) Log.e(clazz.getCanonicalName(), message);
            } catch (Exception ignore) {
                System.err.println(logger + ": " + clazz.getCanonicalName() + ": " + message);
            }
    }
}
