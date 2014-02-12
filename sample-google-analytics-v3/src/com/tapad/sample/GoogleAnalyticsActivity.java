package com.tapad.sample;
import android.app.Activity;
import com.google.analytics.tracking.android.*;
import com.tapad.tapestry.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GoogleAnalyticsActivity extends Activity {
    // Custom Dimension indices that will be used for Tapestry Analytics
    private static final int VISITED_PLATFORMS_DIM_IDX = 1;
    private static final int PLATFORMS_ASSOC_DIM_IDX = 2;
    private static final int PLATFORM_TYPES_DIM_IDX = 3;
    private static final int FIRST_VISITED_DIM_IDX = 4;
    private static final int MOST_RECENT_DIM_IDX = 5;
    private static final int MOST_OFTEN_DIM_IDX = 6;

    @Override
    public void onStart() {
        super.onStart();

        // Initialize tracker
        EasyTracker tracker = EasyTracker.getInstance(this);

        // Start Google Analytics session
        tracker.activityStart(this);

        // Send Tapestry analytics data
        TapestryAnalyticsService.track(tracker, new TapestryClient(this));
    }

    @Override
    public void onStop() {
        // Stop Google Analytics session
        EasyTracker.getInstance(this).activityStop(this);

        super.onStop();
    }

    /**
     * TapestryAnalyticsService can be used from multiple Activities or the Application
     */
    public static class TapestryAnalyticsService {
        private static AtomicLong lastAnalyticsPush = new AtomicLong();

        private static void sendAnalytics(Tracker tracker, Map<String,String> analytics) {
            if (!analytics.isEmpty()) {
                String vp = analytics.get("vp").equals("1") ? analytics.get("vp") + " Platform" : analytics.get("vp") + " Platforms";
                String pa = analytics.get("pa").equals("1") ? analytics.get("pa") + " Platform" : analytics.get("pa") + " Platforms";
                String pt = analytics.get("pt");
                String fvp = analytics.get("fvp");
                String mrvp = analytics.get("mrvp");
                String movp = analytics.get("movp");

                tracker.send(MapBuilder.createEvent("Tapestry", "Visited Platforms", vp, null)
                        .set(Fields.customDimension(VISITED_PLATFORMS_DIM_IDX), vp)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "Platforms Associated", pa, null)
                        .set(Fields.customDimension(PLATFORMS_ASSOC_DIM_IDX), pa)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "Platform Types", pt, null)
                        .set(Fields.customDimension(PLATFORM_TYPES_DIM_IDX), pt)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "First Visited Platform", fvp, null)
                        .set(Fields.customDimension(FIRST_VISITED_DIM_IDX), fvp)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "Most Recent Visited Platform", mrvp, null)
                        .set(Fields.customDimension(MOST_RECENT_DIM_IDX), mrvp)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "Most Often Visited Platform", movp, null)
                        .set(Fields.customDimension(MOST_OFTEN_DIM_IDX), movp)
                        .build());
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
}