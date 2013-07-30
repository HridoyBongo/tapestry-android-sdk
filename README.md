## Quick Setup
Download the Tapestry code.

Copy tapestry.jar into the libs folder of your Android project.

Add your Tapestry Partner Id and permissions to your manifest:
```xml
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

## Using Tapestry
Intialize the TapestryService prior to use, preferably in your Application or in every Activity that uses it:
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
    public void receive(TapestryResponse response) {
        if (response.getData("color").contains("blue"))
          // user has a preference for blue
        if (response.getAudiences().contains("buying-car"))
          // user is in a buying car audience
        if (response.getPlatforms().contains("XBox"))
          // user has an XBox
    }
});
```

Or set Tapestry data:
```java
TapestryService.send(new TapestryRequest()
    .addAudiences("buying-car")
    .addData("color", "blue")
    .userIds("uid", "bob123")
);
```

Or do both at once (setting the depth and strength of the request to change the devices queried):
```java
TapestryRequest = new TapestryRequest()
    .addData("color", "blue")
    .strength(1)
    .depth(2);

TapestryService.send(request, new TapestryCallback()  {
    public void receive(TapestryResponse response) {
        // do stuff
    }
);
```

#### License

Copyright (c) 2012-2013 Tapad, INC.

Published under The MIT License, see LICENSE
