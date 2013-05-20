# Tapestry Android SDK

### Running with IntelliJ
1. Make sure the Anroid plugin is installed in IntelliJ.
2. Go to File > Import Project... > Select the pom.xml file in tapestry-android-sdk.  Go through the dialogs and select the project SDK to be Android.
3. Go to Run > Edit Configurations... > Add new 'Android Application'.  Select module to be 'sample-app' and pick an emulator or device to run on.
4. To run unit tests make sure the Android SDK is below the Maven dependencies, otherwise the classes in android.jar will throw a "Stub!" exception.


# License

Copyright (c) 2012-2013 Tapad, INC.

Published under The MIT License, see LICENSE
