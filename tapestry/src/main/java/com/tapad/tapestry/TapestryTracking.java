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
    public static final String PREF_TAPAD_DEVICE_ID = "_tapad_device_id";
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

    public boolean isOptedOut() {
        return getDeviceId().equals(OPTED_OUT_DEVICE_ID);
    }

    /**
     * Uses the idCollector to generate ids, if any.  This is not done if the user is already opted out through
     * preferences.  If there were no ids generated, a random UUID is generated and persisted through preferences.
     */
    public List<TypedIdentifier> getIds() {
        if (ids == null) {
            ids = new ArrayList<TypedIdentifier>();
            try {
                ids.addAll(source.get(context));
                if (ids.isEmpty())
                    ids.add(new TypedIdentifier(TypedIdentifier.TYPE_RANDOM_UUID, getDeviceId()));
            } catch (Exception e) {
                Logging.warn(getClass(), "Unable to collect ids: " + e.getMessage());
            }
        }
        return ids;
    }

    public String getUserAgent() {
        if (userAgent.isEmpty()) {
            try {
                WebView wv = new WebView(context);
                userAgent = wv.getSettings().getUserAgentString();
                wv.destroy();
            } catch (Exception e) {
                Logging.error(getClass(), "Could not get user agent", e);
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

    protected SharedPreferences getPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences == null)
            throw new IllegalStateException("Preferences is null, make sure onCreate() has been called before using Tapestry");
        return preferences;
    }
}