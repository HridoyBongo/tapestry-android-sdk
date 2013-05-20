package com.tapad.tapestry;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import com.tapad.tracking.deviceidentification.IdentifierSource;
import com.tapad.tracking.deviceidentification.ManifestAggregator;
import com.tapad.tracking.deviceidentification.TypedIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gets hardware ids from this device for {@link TapestryClient}.
 */
public class TapestryTracking {
    private final List<TypedIdentifier> ids = new ArrayList<TypedIdentifier>();
    private String userAgent = "";
    private String deviceId;

    public TapestryTracking(Context context) {
        this(context, new ManifestAggregator());
    }

    public TapestryTracking(Context context, IdentifierSource source) {
        updateDeviceId(context);
        try {
            ids.addAll(source.get(context));
            if (ids.isEmpty() && getDeviceId() != null)
                ids.add(new TypedIdentifier(TypedIdentifier.TYPE_RANDOM_UUID, getDeviceId()));
        } catch (Exception e) {
            Logging.error(getClass(), "Unable to collect ids", e);
        }
        try {
            WebView wv = new WebView(context);
            userAgent = wv.getSettings().getUserAgentString();
            wv.destroy();
        } catch (Exception e) {
            Logging.error(getClass(), "Could not get user agent", e);
        }
        // We throw an uncaught exception here because we don't want to fail silently due to an easy-to-make mistake
        if (ids.isEmpty())
            throw new RuntimeException("Tapestry cannot identify this device, make sure onCreate() has been called before instantiating TapestryClient");
    }

    /**
     * Uses the idCollector to generate ids, if any.  This is not done if the user is already opted out through
     * preferences.  If there were no ids generated, a random UUID is generated and persisted through preferences.
     */
    public List<TypedIdentifier> getIds() {
        return ids;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void updateDeviceId(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            deviceId = preferences.getString(TapestryClient.PREF_TAPAD_DEVICE_ID, null);
            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString();
                preferences.edit().putString(TapestryClient.PREF_TAPAD_DEVICE_ID, deviceId).commit();
            }
        } catch (Exception e) {
            Logging.error(getClass(), "Error updating device id", e);
        }
    }
}