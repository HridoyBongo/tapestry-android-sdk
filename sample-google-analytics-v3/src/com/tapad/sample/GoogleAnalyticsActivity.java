package com.tapad.sample;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.tapad.tapestry.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GoogleAnalyticsActivity extends Activity {
    private String GA_PROPERTY_ID = getMetaData(this, "ga.PROPERTY_ID", "UA-30562281-7");

    /**
     * TapestryAnalyticsService can be used from multiple Activities or the Application
     */
    public static class TapestryAnalyticsService {
        private static AtomicLong lastAnalyticsPush = new AtomicLong();

        private static void sendAnalytics(Tracker tracker, Map<String,String> analytics) {
            if (!analytics.isEmpty()) {
                String vp = analytics.get("vp").equals("1") ? analytics.get("vp") + " Platform" : analytics.get("vp") + " Platforms";
                String pa = analytics.get("pa").equals("1") ? analytics.get("pa") + " Platform" : analytics.get("pa") + " Platforms";
                tracker.set(Fields.customDimension(1), vp); // Visited Platforms
                tracker.set(Fields.customDimension(2), pa); // Platforms Associated
                tracker.set(Fields.customDimension(3), analytics.get("pt")); // Platform Types
                tracker.set(Fields.customDimension(4), analytics.get("fvp")); // First Visited Platform
                tracker.set(Fields.customDimension(5), analytics.get("mrvp")); // Most Recent Visited Platform
                if (analytics.get("movp") != null)
                    tracker.set(Fields.customDimension(6), analytics.get("movp")); // Most Often Visited Platform
                // manual dispatch
                GAServiceManager.getInstance().dispatchLocalHits();
            }
        }

        public static void track(final Tracker tracker, TapestryClient tapestry) {
            boolean isNewSession = lastAnalyticsPush.get() < System.currentTimeMillis() - 30 * 60 * 1000;
            lastAnalyticsPush.set(System.currentTimeMillis());
            tapestry.send(new TapestryRequest().analytics(isNewSession), new TapestryCallback() {
                @Override
                public void receive(TapestryResponse response, Exception exception, long millisSinceInvocation) {
                    sendAnalytics(tracker, response.analytics());
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracker tracker = GoogleAnalytics.getInstance(this).getTracker(GA_PROPERTY_ID);
        TapestryAnalyticsService.track(tracker, new TapestryClient(this));
    }


    private static String getMetaData(Context context, String key, String defaultValue) {
        try {
            String value = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.get(key).toString();
            return value == null ? defaultValue : value;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}