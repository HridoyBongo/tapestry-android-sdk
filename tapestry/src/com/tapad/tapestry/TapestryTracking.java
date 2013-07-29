package com.tapad.tapestry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.tapad.tracking.deviceidentification.AndroidId;
import com.tapad.tracking.deviceidentification.IdentifierSource;
import com.tapad.tracking.deviceidentification.IdentifierSourceAggregator;
import com.tapad.tracking.deviceidentification.PhoneId;
import com.tapad.tracking.deviceidentification.TypedIdentifier;
import com.tapad.tracking.deviceidentification.UserAgent;
import com.tapad.tracking.deviceidentification.WifiMac;

/**
 * Gets hardware ids from this device for {@link TapestryClient}.
 */
public class TapestryTracking {
    // Platforms we can identify in the user agent to avoid sending one explicitly
    private static final List<String> PLATFORMS = Arrays.asList("smarttv", "googletv", "internet.tv", "netcast", "nettv", "appletv", "boxee", "kylo", "roku", "dlnadoc", "ce-html", "symbian", "kindle", "android", "blackberry", "palm", "wii", "playstation", "xbox", "silk", "msie");
    // Defined in Android SDKs newer than 1.6
    private static final int SCREENLAYOUT_SIZE_XLARGE = 4;
    private final List<TypedIdentifier> ids = new ArrayList<TypedIdentifier>();
    private String userAgent = "";
    private String deviceId = "";
    private String platform = "";

    public TapestryTracking(Context context) {
        this(context, new IdentifierSourceAggregator(Arrays.asList(new AndroidId(), new WifiMac(), new PhoneId())));
    }

    public TapestryTracking(Context context, IdentifierSource source) {
        updateDeviceId(context);
        try {
            ids.addAll(source.get(context));
            if (ids.isEmpty() && getDeviceId() != null)
                ids.add(new TypedIdentifier(TypedIdentifier.TYPE_RANDOM_UUID, getDeviceId()));
        } catch (Exception e) {
            Logging.e("Unable to collect ids", e);
        }
        try {
            userAgent = UserAgent.getUserAgent(context);
        } catch (Exception e) {
            Logging.e("Could not get user agent", e);
        }
        try {
            platform = identifyPlatform(context);
        } catch (Exception e) {
            Logging.e("Could not identify platform", e);
        }
        // We throw an uncaught exception here because we don't want to fail silently due to an easy-to-make mistake
        if (ids.isEmpty())
            throw new RuntimeException("Tapestry cannot identify this device, make sure onCreate() has been called before instantiating TapestryClient");
    }

    @SuppressLint("DefaultLocale")
	private String identifyPlatform(Context context) {
        String userAgentLower = userAgent.toLowerCase();
        for (String platform : PLATFORMS)
            if (userAgentLower.contains(platform))
                return "";
        int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == SCREENLAYOUT_SIZE_XLARGE)
            return "android tablet";
        return "android mobile";
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

    public String getPlatform() {
        return platform;
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
            Logging.e("Error updating device id", e);
        }
    }
}