package com.tapad.tapestry;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import com.tapad.tracking.deviceidentification.IdentifierSource;
import com.tapad.tracking.deviceidentification.IdentifierSourceAggregator;
import com.tapad.tracking.deviceidentification.TypedIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TapestryTracking {
    private static final String PREF_TAPAD_DEVICE_ID = "_tapad_device_id";
    public static final String OPTED_OUT_DEVICE_ID = "OptedOut";
    private final IdentifierSource source;
    private final Context context;
    private List<TypedIdentifier> ids;

    public static List<IdentifierSource> createIdentifierSourcesFromManifest(Context context) {
        ArrayList<IdentifierSource> sources = new ArrayList<IdentifierSource>();
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String[] idSourceClasses = ai.metaData.getString("tapad.ID_SOURCES").split(",");
            for (String className : idSourceClasses) {
                sources.add((IdentifierSource) Class.forName("com.tapad.tracking.deviceidentification." + className.trim()).newInstance());
            }
        } catch (Exception e) {
            com.tapad.util.Logging.warn("TapestryTracking", "Unable to instantiate identifier sources from manifest: " + e.getMessage());
        }
        return sources;
    }

    public TapestryTracking(Context context) {
        this(context, createIdentifierSourcesFromManifest(context));
    }

    public TapestryTracking(Context context, List<IdentifierSource> sources) {
        this.context = context;
        this.source = new IdentifierSourceAggregator(sources);
        ids = updateIds();
    }

    /**
     * Opts the device back in after an opt out.
     */
    public void optIn() {
        // we clear the saved preferences and run through id collection logic once more
        PreferenceManager.getDefaultSharedPreferences(context)
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
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_TAPAD_DEVICE_ID, OPTED_OUT_DEVICE_ID)
                .commit();
        ids = updateIds();
    }

    public boolean isOptedOut() {
        return getDeviceId().equals(OPTED_OUT_DEVICE_ID);
    }

    public List<TypedIdentifier> getIds() {
        return ids;
    }

    public String getUserAgent() {
        WebView wv = new WebView(context);
        String userAgent = wv.getSettings().getUserAgentString();
        wv.destroy();
        return userAgent;
    }

    public String getDeviceId() {
        String deviceId = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_TAPAD_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_TAPAD_DEVICE_ID, deviceId).commit();
        }
        return deviceId;
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