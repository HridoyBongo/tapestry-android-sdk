package com.tapad.sample;

import android.app.Application;

import com.tapad.tapestry.TapestryService;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        // The application is always called before an activity so it is the place to initialize Tapestry
        TapestryService.initialize(this);
    }
}
