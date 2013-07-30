package com.tapad.tapestry;

import android.content.Context;

/**
 * The TapestryService is a singleton (static) wrapper for the {@link TapestryClient} to provide one point of
 * initialization.  The service should be initialized in your application's {@code onCreate()} method.  If you have no
 * main {@code Application} then it must be initialized in every entry point of your app.
 * <p/>
 * The client sends a {@link TapestryRequest} as an HTTP request to the Tapestry Web API and parses the JSON response as
 * {@link TapestryResponse}.  When calling the client from the Android UI thread, it is recommended to send requests
 * asynchronously by passing in a {@link TapestryCallback} so as not to block the UI thread.
 * <p/>
 * An example of sending a request and receiving an asynchronous response:
 * <blockquote><pre>
 * TapestryService.initialize(context);
 * TapestryRequest request = new TapestryRequest();
 * // TODO build request
 * TapestryService.send(request, new TapestryCallback() {
 *     {@literal @}Override
 *     public void receive(TapestryResponse response) {
 *         // TODO handle response
 *     }
 * });
 * </pre></blockquote>
 * Note if the callback updates the UI then you should use the {@link TapestryUICallback} convenience class which runs
 * the callback on the UI thread.  The client throws no exceptions unless {@link Logging#setThrowExceptions(boolean)}
 * has been set to true.
 */
public class TapestryService {
    private static TapestryClient client = null;

    /**
     * Creates client ready to receive requests.  Client cannot be instantiated before {@code onCreate} is called.
     * Partner id will be read from {@code tapad.PARTNER_ID} in the manifest or default if none exists.  The url of the
     * API will be read from {@code tapad.API_URL} in the manifest or default if none exists.
     *
     * @param context The context of the app
     */
    public static void initialize(Context context) {
        client = new TapestryClient(context);
    }

    /**
     * Creates client ready to receive requests.  Client cannot be instantiated before {@code onCreate} is called.  The
     * url of the API will be read from {@code tapad.API_URL} in the manifest or default if none exists.
     *
     * @param context   The context of the app
     * @param partnerId The Tapestry partner id that has been assigned to you
     */
    public static void initialize(Context context, String partnerId) {
        client = new TapestryClient(context, partnerId);
    }

    /**
     * Creates client ready to receive requests.  Client cannot be instantiated before {@code onCreate} is called.
     *
     * @param context   The context of the app
     * @param partnerId The Tapestry partner id that has been assigned to you
     * @param url       The url of the tapestry host
     */
    public static void initialize(Context context, String partnerId, String url) {
        client = new TapestryClient(new TapestryTracking(context), partnerId, url);
    }

    /**
     * Get an instance of the underlying client.  Throws a {@code RuntimeException} if not initialized.
     *
     * @return the Tapestry client
     */
    public static TapestryClient client() {
        if (client == null)
            throw new RuntimeException("Tapestry service must be initialized before use.");
        return client;
    }

    /**
     * Sends a request asynchronously using a worker thread pool, without returning a response.
     *
     * @param request The request
     */
    public static void send(final TapestryRequest request) {
        client().send(request);
    }

    /**
     * Sends a request asynchronously using a worker thread pool.
     *
     * @param callback A callback that will be called when the Tapestry server responds
     */
    public static void send(final TapestryCallback callback) {
        client().send(callback);
    }

    /**
     * Sends a request asynchronously using a worker thread pool.
     *
     * @param request  The request
     * @param callback A callback that will be called when the Tapestry server responds
     */
    public static void send(final TapestryRequest request, final TapestryCallback callback) {
        client().send(request, callback);
    }

    /**
     * Sends a request synchronously, blocking until response is received.
     *
     * @param request The request
     * @return a response from the server
     */
    public TapestryResponse sendSynchronously(TapestryRequest request) {
        return client().sendSynchronously(request);
    }

    /**
     * Opts this device into tracking.  Tapestry will collect ids and send requests from this app.
     */
    public static void optIn(Context context) {
        client().optIn(context);
    }

    /**
     * Opts this device out of all tracking. Tapestry will not be able to send requests from this app.  All responses
     * will be a {@link TapestryResponse} containing an {@link TapestryError#OPTED_OUT} error.
     */
    public static void optOut(Context context) {
        client().optOut(context);
    }
}
