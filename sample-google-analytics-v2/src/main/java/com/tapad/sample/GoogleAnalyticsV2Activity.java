package com.tapad.sample;

import android.app.Activity;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

public class GoogleAnalyticsV2Activity extends Activity {
    @Override
    protected void onStart() {
        super.onStart();
        Tracker tracker = GoogleAnalytics.getInstance(this).getTracker("UA-41283710-2");
        GAServiceManager.getInstance().dispatch();
    }
}
