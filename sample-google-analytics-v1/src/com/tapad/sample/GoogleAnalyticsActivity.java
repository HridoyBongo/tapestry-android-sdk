package com.tapad.sample;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.tapad.tapestry.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GoogleAnalyticsActivity extends Activity {
    private final String GA_PROPERTY_ID = getMetaData(this, "ga.PROPERTY_ID", null);

    /**
     * TapestryAnalyticsService can be used from multiple Activities or the Application
     */
    public static class TapestryAnalyticsService {
        private static AtomicLong lastAnalyticsPush = new AtomicLong();

        private static void sendAnalytics(GoogleAnalyticsTracker tracker, Map<String,String> analytics) {
            if (!analytics.isEmpty()) {
                String vp = analytics.get("vp").equals("1") ? analytics.get("vp") + "_Platform" : analytics.get("vp") + "_Platforms";
                String pa = analytics.get("pa").equals("1") ? analytics.get("pa") + "_Platform" : analytics.get("pa") + "_Platforms";
                // You can modify the custom variables and scope here (2 = session-level scope)
                tracker.setCustomVar(1, "Visited_Platforms", vp, 2);
                tracker.setCustomVar(2, "Platforms_Associated", pa, 2);
                tracker.setCustomVar(3, "Platform_Types", analytics.get("pt"), 2);
                tracker.setCustomVar(4, "First_Visited_Platform", analytics.get("fvp"), 2);
                tracker.setCustomVar(5, "Most_Recent_Visited_Platform", analytics.get("mrvp"), 2);
                if (analytics.get("movp") != null)
                    tracker.setCustomVar(6, "Most_Often_Visited_Platform", analytics.get("movp"), 2);
                tracker.trackEvent("tapestry", "android", "", 0);
                tracker.dispatch();
            }
        }

        /**
         * Retrieves Tapestry analytics and sends to Google Analytics.
         * @param tracker   a Google Analytics tracker
         * @param tapestry  a Tapestry client
         */
        public static void track(final GoogleAnalyticsTracker tracker, TapestryClient tapestry) {
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
        GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession(GA_PROPERTY_ID, this);
        tracker.setDebug(true);
        TapestryAnalyticsService.track(tracker, new TapestryClient(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalyticsTracker.getInstance().stopSession();
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