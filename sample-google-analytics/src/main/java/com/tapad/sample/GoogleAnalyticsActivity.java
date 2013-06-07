package com.tapad.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.tapad.tapestry.*;

import java.util.concurrent.atomic.AtomicLong;

public class GoogleAnalyticsActivity extends Activity {

    // Service can be used from multiple Activities or the Application
    public static class TapestryAnalyticsService {
        private static AtomicLong lastAnalyticsPush = new AtomicLong();

        public static void track(GoogleAnalyticsTracker tracker, TapestryClient tapestry) {
            boolean isNewSession = lastAnalyticsPush.get() < System.currentTimeMillis() - 30 * 60 * 1000;
            tapestry.send(new TapestryRequest().analytics(isNewSession), new TapestryCallback() {
                @Override
                public void receive(TapestryResponse response) {

                }
            });
            GoogleAnalyticsTracker.getInstance().setDebug(true);
            GoogleAnalyticsTracker.getInstance().setCustomVar(1, "Test", "var", 1);
            GoogleAnalyticsTracker.getInstance().dispatch();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        tracker.startNewSession("UA-41283710-1", this);
        TapestryAnalyticsService.track(tracker, new TapestryClient(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalyticsTracker.getInstance().stopSession();
    }
}