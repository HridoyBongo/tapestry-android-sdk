package com.tapad.sample;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.tapad.tapestry.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TapestryAnalyticsPlugin can be used from multiple Activities or the Application
 */
public class TapestryAnalyticsPlugin {
    private static AtomicLong lastAnalyticsPush = new AtomicLong();

    private static void sendAnalytics(GoogleAnalyticsTracker tracker, Map<String,String> analytics) {
        if (!analytics.isEmpty()) {
            Logging.d("Received analytics: vp : " + analytics.get("vp") +
                ", pa: " + analytics.get("pa") +
                ", pt: " + analytics.get("pt") +
                ", fvp: " + analytics.get("fvp") +
                ", mrvp: " + analytics.get("mrvp") +
                ", movp: " + analytics.get("movp"));
            String vp = analytics.get("vp").equals("1") ? analytics.get("vp") + "_Platform" : analytics.get("vp") + "_Platforms";
            String pa = analytics.get("pa").equals("1") ? analytics.get("pa") + "_Platform" : analytics.get("pa") + "_Platforms";
            // You can modify the custom variables and scope here (2 = session-level scope)
            tracker.setCustomVar(1, "Visited_Platforms", vp, 2);
            tracker.trackEvent("tapestry_vp", "Visited_Platforms", "", 0);
            tracker.dispatch();
            tracker.setCustomVar(1, "Platforms_Associated", pa, 2);
            tracker.trackEvent("tapestry_pa", "Platforms_Associated", "", 0);
            tracker.dispatch();
            tracker.setCustomVar(1, "Platform_Types", analytics.get("pt"), 2);
            tracker.trackEvent("tapestry_pt", "Platform_Types", "", 0);
            tracker.dispatch();
            tracker.setCustomVar(1, "First_Visited_Platform", analytics.get("fvp"), 2);
            tracker.trackEvent("tapestry_fvp", "First_Visited_Platform", "", 0);
            tracker.dispatch();
            tracker.setCustomVar(1, "Most_Recent_Visited_Platform", analytics.get("mrvp"), 2);
            tracker.trackEvent("tapestry_mrvp", "Most_Recent_Visited_Platform", "", 0);
            tracker.dispatch();
            if (analytics.get("movp") != null) {
                tracker.setCustomVar(1, "Most_Often_Visited_Platform", analytics.get("movp"), 2);
                tracker.trackEvent("tapestry_movp", "Most_Often_Visited_Platform", "", 0);
                tracker.dispatch();
            }
        }
    }

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
