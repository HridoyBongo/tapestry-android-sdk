package com.tapad.sample;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.tapad.tapestry.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GoogleAnalyticsActivity extends Activity {
    /**
     * TapestryAnalyticsService can be used from multiple Activities or the Application
     */
    public static class TapestryAnalyticsService {
        private static AtomicLong lastAnalyticsPush = new AtomicLong();

        private static void sendAnalytics(GoogleAnalyticsTracker tracker, Map<String,String> analytics) {
            if (!analytics.isEmpty()) {
                // You can modify the custom variables and scope here (2 = session-level scope)
                tracker.setCustomVar(1, "Screen Count", analytics.get("sc"), 2);
                tracker.setCustomVar(2, "Platforms Associated", analytics.get("pa"), 2);
                tracker.setCustomVar(3, "Platform Types", analytics.get("pt"), 2);
                tracker.setCustomVar(4, "First Visited Platform", analytics.get("fvp"), 2);
                // Because there can only be 5 custom variables the following is unused
                tracker.setCustomVar(6, "Most Recent Visited Platform", analytics.get("mrvp"), 2);
                if (analytics.get("movp") != null)
                    tracker.setCustomVar(5, "Most Often Visited Platform", analytics.get("movp"), 2);
                tracker.trackEvent("tapestry", "android", "", 0);
                tracker.dispatch();
                lastAnalyticsPush.set(System.currentTimeMillis());
            }
        }

        public static void track(final GoogleAnalyticsTracker tracker, TapestryClient tapestry) {
            boolean isNewSession = lastAnalyticsPush.get() < System.currentTimeMillis() - 30 * 60 * 1000;
            tapestry.send(new TapestryRequest().analytics(isNewSession), new TapestryCallback() {
                @Override
                public void receive(TapestryResponse response) {
                    sendAnalytics(tracker, response.analytics());
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession("UA-41283710-1", this);
        tracker.setDebug(true);
        TapestryAnalyticsService.track(tracker, new TapestryClient(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalyticsTracker.getInstance().stopSession();
    }
}