package com.tapad.tapestry;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import com.tapad.tracking.deviceidentification.IdentifierSource;
import com.tapad.tracking.deviceidentification.ManifestAggregator;
import com.tapad.tracking.deviceidentification.TypedIdentifier;
import com.tapad.util.Logging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TapestryTracking {
    private static final String PREF_TAPAD_DEVICE_ID = "_tapad_device_id";
    public static final String OPTED_OUT_DEVICE_ID = "OptedOut";
    private final IdentifierSource source;
    private final Context context;
    private String userAgent = "";
    private List<TypedIdentifier> ids;

    public TapestryTracking(Context context) {
        this(context, new ManifestAggregator());
    }

    public TapestryTracking(Context context, IdentifierSource source) {
        this.context = context;
        this.source = source;
    }

    /**
     * Opts the device back in after an opt out.
     */
    public void optIn() {
        // we clear the saved preferences and run through id collection logic once more
        getPreferences()
                .edit()
                .remove(PREF_TAPAD_DEVICE_ID)
                .commit();
        ids = updateIds();
    }

    /**
     * Opts the device out of all tracking / personalization by setting the device id to the constant
     * string OptedOut. This means that it is now impossible to distinguish this device from all
     * other opted out device.
     */
    public void optOut() {
        getPreferences()
                .edit()
                .putString(PREF_TAPAD_DEVICE_ID, OPTED_OUT_DEVICE_ID)
                .commit();
        ids = updateIds();
    }

    public boolean isOptedOut() {
        return getDeviceId().equals(OPTED_OUT_DEVICE_ID);
    }

    public List<TypedIdentifier> getIds() {
        if (ids == null)
            ids = updateIds();
        return ids;
    }

    public String getUserAgent() {
        if (userAgent.isEmpty()) {
            try {
                WebView wv = new WebView(context);
                userAgent = wv.getSettings().getUserAgentString();
                wv.destroy();
            } catch (Exception e) {
                Logging.error("TapestryTracking", "Could not get user agent", e);
            }
        }
        return userAgent;
    }

    public String getDeviceId() {
        String deviceId = getPreferences().getString(PREF_TAPAD_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            getPreferences().edit().putString(PREF_TAPAD_DEVICE_ID, deviceId).commit();
        }
        return deviceId;
    }

    private SharedPreferences getPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences == null)
            throw new IllegalStateException("Preferences is null, make sure onCreate() has been called before using Tapestry");
        return preferences;
    }

    /**
     * Uses the idCollector to generate ids, if any.  This is not done if the user is already opted out through
     * preferences.  If there were no ids generated, a random UUID is generated and persisted through
     * preferences.
     */
    private List<TypedIdentifier> updateIds() {
        List<TypedIdentifier> ids = new ArrayList<TypedIdentifier>();
        // do not attempt to collect any ids if the device is opted out
        if (isOptedOut())
            return ids;

        // collect ids
        try {
            ids.addAll(source.get(context));
            if (ids.isEmpty())
                ids.add(new TypedIdentifier(TypedIdentifier.TYPE_RANDOM_UUID, getDeviceId()));
        } catch (Exception e) {
            Logging.warn("TapestryTracking", "Unable to collect ids: " + e.getMessage());
        }
        return ids;
    }
}