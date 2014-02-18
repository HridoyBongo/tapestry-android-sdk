package com.tapad.sample;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import com.google.analytics.tracking.android.*;
import com.tapad.tapestry.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GoogleAnalyticsActivity extends Activity {

    @Override
    public void onStart() {
        super.onStart();

        // Initialize tracker
        EasyTracker tracker = EasyTracker.getInstance(this);

        // Start Google Analytics session
        tracker.activityStart(this);

        // Send Tapestry analytics data
        TapestryAnalyticsService.track(tracker, new CustomDimensionConfig(), new TapestryClient(this));
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

        private static void sendAnalytics(Tracker tracker, CustomDimensionConfig dimensions, Map<String,String> analytics) {
            if (!analytics.isEmpty()) {
                String vp = analytics.get("vp").equals("1") ? analytics.get("vp") + " Platform" : analytics.get("vp") + " Platforms";
                String pa = analytics.get("pa").equals("1") ? analytics.get("pa") + " Platform" : analytics.get("pa") + " Platforms";
                String pt = analytics.get("pt");
                String fvp = analytics.get("fvp");
                String mrvp = analytics.get("mrvp");
                String movp = analytics.get("movp");

                tracker.send(MapBuilder.createEvent("Tapestry", "Visited Platforms", vp, null)
                        .set(Fields.customDimension(dimensions.getVisitedPlatformsIdx()), vp)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "Platforms Associated", pa, null)
                        .set(Fields.customDimension(dimensions.getPlatformsAssociatedIdx()), pa)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "Platform Types", pt, null)
                        .set(Fields.customDimension(dimensions.getPlatformTypesIdx()), pt)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "First Visited Platform", fvp, null)
                        .set(Fields.customDimension(dimensions.getFirstVisitedPlatformIdx()), fvp)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "Most Recent Visited Platform", mrvp, null)
                        .set(Fields.customDimension(dimensions.getMostRecentPlatformIdx()), mrvp)
                        .build());
                tracker.send(MapBuilder.createEvent("Tapestry", "Most Often Visited Platform", movp, null)
                        .set(Fields.customDimension(dimensions.getMostOftenPlatformIdx()), movp)
                        .build());
            }
        }

        /**
         * Retrieves Tapestry analytics and sends to Google Analytics.
         * @param tracker    a Google Analytics tracker
         * @param dimensions a CustomDimensionConfig object that will be used to retrieve dimension indices
         * @param tapestry   a Tapestry client
         */
        public static void track(final Tracker tracker, final CustomDimensionConfig dimensions, TapestryClient tapestry) {

            boolean isNewSession = lastAnalyticsPush.get() < System.currentTimeMillis() - 30 * 60 * 1000;
            lastAnalyticsPush.set(System.currentTimeMillis());
            tapestry.send(new TapestryRequest().analytics(isNewSession), new TapestryCallback() {
                @Override
                public void receive(TapestryResponse response, Exception exception, long millisSinceInvocation) {
                    sendAnalytics(tracker, dimensions, response.analytics());
                }
            });
        }
    }

    /**
     *  Custom Dimension indices that will be used for Tapestry Analytics
     */
    public class CustomDimensionConfig {

        /**
         * Retrieves the index for the Visited Platforms custom dimension in Google Analytics.
         * This index must be configured in the AndroidManifest.xml property named ga.VISITED_PLATFORMS_DIM_IDX,
         * and should match the custom dimension index used in your Google Analytics property configuration.
         * If the field is omitted, a default value of 0 is used, which is ignored by Google Analytics.
         * @return the index for Visited Platforms custom dimension
         */
        public final int getVisitedPlatformsIdx(){
            return Integer.parseInt(getMetaData(GoogleAnalyticsActivity.this, "ga.VISITED_PLATFORMS_DIM_IDX", "0"));
        }

        /**
         * Retrieves the index for the Platforms Associated custom dimension in Google Analytics.
         * This index must be configured in the AndroidManifest.xml property named ga.PLATFORMS_ASSOC_DIM_IDX,
         * and should match the custom dimension index used in your Google Analytics property configuration.
         * If the field is omitted, a default value of 0 is used, which is ignored by Google Analytics.
         * @return the index for Platforms Associated custom dimension
         */
        public final int getPlatformsAssociatedIdx(){
            return Integer.parseInt(getMetaData(GoogleAnalyticsActivity.this, "ga.PLATFORMS_ASSOC_DIM_IDX", "0"));
        }

        /**
         * Retrieves the index for the Platform Types custom dimension in Google Analytics.
         * This index must be configured in the AndroidManifest.xml property named ga.PLATFORM_TYPES_DIM_IDX,
         * and should match the custom dimension index used in your Google Analytics property configuration.
         * If the field is omitted, a default value of 0 is used, which is ignored by Google Analytics.
         * @return the index for Platforms Types custom dimension
         */
        public final int getPlatformTypesIdx(){
            return Integer.parseInt(getMetaData(GoogleAnalyticsActivity.this, "ga.PLATFORM_TYPES_DIM_IDX", "0"));
        }

        /**
         * Retrieves the index for the First Visited Platform custom dimension in Google Analytics.
         * This index must be configured in the AndroidManifest.xml property named ga.FIRST_VISITED_DIM_IDX,
         * and should match the custom dimension index used in your Google Analytics property configuration.
         * If the field is omitted, a default value of 0 is used, which is ignored by Google Analytics.
         * @return the index for First Visited Platform custom dimension
         */
        public final int getFirstVisitedPlatformIdx(){
            return Integer.parseInt(getMetaData(GoogleAnalyticsActivity.this, "ga.FIRST_VISITED_DIM_IDX", "0"));
        }

        /**
         * Retrieves the index for the Most Recent Visited Platform custom dimension in Google Analytics.
         * This index must be configured in the AndroidManifest.xml property named ga.MOST_RECENT_DIM_IDX,
         * and should match the custom dimension index used in your Google Analytics property configuration.
         * If the field is omitted, a default value of 0 is used, which is ignored by Google Analytics.
         * @return the index for Most Recent Visited Platform custom dimension
         */
        public final int getMostRecentPlatformIdx(){
            return Integer.parseInt(getMetaData(GoogleAnalyticsActivity.this, "ga.MOST_RECENT_DIM_IDX", "0"));
        }

        /**
         * Retrieves the index for the Most Often Visited Platform custom dimension in Google Analytics.
         * This index must be configured in the AndroidManifest.xml property named ga.MOST_OFTEN_DIM_IDX,
         * and should match the custom dimension index used in your Google Analytics property configuration.
         * If the field is omitted, a default value of 0 is used, which is ignored by Google Analytics.
         * @return the index for Most Often Visited Platform custom dimension
         */
        public final int getMostOftenPlatformIdx(){
            return Integer.parseInt(getMetaData(GoogleAnalyticsActivity.this, "ga.MOST_OFTEN_DIM_IDX", "0"));
        }
    }

    private static String getMetaData(Context context, String key, String defaultValue) {
        try {
            String value = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.get(key).toString();
            return value == null ? defaultValue : value;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}