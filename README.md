## Quick Setup ## {#quick-setup}
1. Download the [Tapestry SDK](https://github.com/Tapad/tapestry-android-sdk/releases) (tapestry-android-sdk-X-X-X.zip) from the releases page ([JavaDoc here](https://tapad.github.com/tapestry-android-sdk/docs)).

2. Copy `tapestry-android-sdk-X-X-X.jar` into the `libs` folder of your Android project.

3. Add your Tapestry Partner Id and permissions into the `AndroidManifest.xml` of your application:
```xml
    <!-- Optional permissions for detecting phone's connectivity and caching requests when offline -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Permissions for accessing the phone's ids (at least one is required) -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <application>
        ...
      <!-- Your Tapestry partner id will be provided by Tapad -->
        <meta-data android:name="tapad.PARTNER_ID" android:value="1" />
    </application>
```

## Using Tapestry ## {#using-tapestry}
Intialize the TapestryService prior to use, preferably in your `Application` or in every `Activity` that uses it:
```java
public class MyApplication extends Application {
    public void onCreate() {
        TapestryService.initialize(this);
    }
}
```

Now you can get cross-device data out of Tapestry:
```java
TapestryService.send(new TapestryCallback() {
    public void receive(TapestryResponse response, Exception exception, long millisSinceInvocation) {
        if (response.getData("color").contains("blue"))
          // user has a preference for blue
        if (response.getAudiences().contains("buying-car"))
          // user is in a buying car audience
        if (response.getPlatforms().contains("XBox"))
          // user has an XBox
    }
});
```

Or set the Tapestry data of the current device:
```java
TapestryService.send(new TapestryRequest()
    .addAudiences("buying-car")
    .addData("color", "blue")
    .userIds("uid", "bob123")
);
```

Or do both at once:
```java
TapestryRequest = new TapestryRequest()
    .addData("color", "blue")
    .strength(1)
    .depth(2);

TapestryService.send(request, new TapestryCallback()  {
    public void receive(TapestryResponse response, Exception exception, long millisSinceInvocation) {
        // do stuff
    }
);
```

## Using Tapestry Google Analytics Plugin

### v1 (Legacy)

Use this version of the Tapestry plugin if your android app currently uses the Google Analytics SDK for Android v1. The Tapestry plugin uses up to 6 custom variable slots in Google Analytics. To take advantage of all 6 analytics, users will either need a Premium Google Analytics account or a Universal property implemented with v3 of the SDK.

To start using the plugin, first complete the instructions in [Quick Setup](#quick-setup).

Next, add your Google Analytics property id to the AndroidManifest.xml:
```xml
    <application>
    ...
    <!-- Your Tapestry partner id will be provided by Tapad -->
    <meta-data android:name="tapad.PARTNER_ID" android:value="1"/>
        
    <!-- Place your Google Analytics property id here -->
    <meta-data android:name="ga.PROPERTY_ID" android:value="UA-XXXXXXXX-X"/>
    </application>
```
Ensure that the TapestryService has been initialized, as detailed in [Using Tapestry](#using-tapestry).

See `GoogleAnalyticsActivity.java` for an example Activity that utilizes the plugin. The code provided in this example should be added to the Activity that you would like to use to send analytics.

Note: This example does NOT use EasyTracker. If your analytics code has been implemented with EasyTracker you will need to make the appropriate adjustments to the provided code.

## License

Copyright (c) 2012-2013 Tapad, INC.

Published under The MIT License, see LICENSE

