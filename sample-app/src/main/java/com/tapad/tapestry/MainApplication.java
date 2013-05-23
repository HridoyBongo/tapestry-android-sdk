package com.tapad.tapestry;

import android.app.Application;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        // The application is always called before an activity so it is the place to initialize Tapestry
        TapestryService.initialize(this);
    }
}
