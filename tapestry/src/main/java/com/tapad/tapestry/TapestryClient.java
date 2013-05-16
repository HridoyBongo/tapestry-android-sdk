package com.tapad.tapestry;

import android.content.Context;
import com.tapad.tracking.deviceidentification.TypedIdentifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.tapad.tapestry.TapestryError.CLIENT_REQUEST_ERROR;
import static com.tapad.tapestry.TapestryError.OPTED_OUT;
import static com.tapad.tapestry.TapestryTracking.OPTED_OUT_DEVICE_ID;
import static com.tapad.tapestry.TapestryTracking.PREF_TAPAD_DEVICE_ID;

/**
 * A client for sending requests to the Tapestry Web API and returning responses.
 * <p/>
 * The client sends a {@link TapestryRequest} as an HTTP request to the Tapestry Web API and parses the JSON response as
 * {@link TapestryResponse}.  When calling the client from the Android UI thread, it is recommended to send requests
 * asynchronously by passing in a {@link TapestryCallback} so as not to block the UI thread.
 * <p/>
 * An example of sending a request and receiving an asynchronous response:
 * <blockquote><pre>
 * TapestryClient client = new TapestryClient(context, "your-partner-id");
 * TapestryRequest request = new TapestryRequest();
 * // TODO build request
 * client.send(request, new TapestryCallback() {
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
public class TapestryClient {
    private final TapestryTracking tracking;
    private final String url;
    private final String partnerId;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * Creates client ready to receive requests.  Can be instantiated before {@code onCreate} is called.
     *
     * @param context   The context of the app
     * @param partnerId The Tapestry partner id that has been assigned to you
     */
    public TapestryClient(Context context, String partnerId) {
        this(new TapestryTracking(context), partnerId, "http://tapestry.tapad.com/tapestry/1");
    }

    public TapestryClient(TapestryTracking tracking, String partnerId, String url) {
        this.tracking = tracking;
        this.partnerId = partnerId;
        this.url = url;
    }

    /**
     * Sends a request asynchronously using a worker thread pool.
     *
     * @param request  The request
     * @param callback A callback that will be called when the Tapestry server responds
     */
    public void send(final TapestryRequest request, final TapestryCallback callback) {
        tracking.getUserAgent();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                callback.receive(sendSynchronously(request));
            }
        });
    }

    /**
     * Sends a request synchronously, blocking until response is received.
     *
     * @param request The request
     * @return a response from the server
     */
    public TapestryResponse sendSynchronously(TapestryRequest request) {
        try {
            if (tracking.isOptedOut())
                return new TapestryResponse(new TapestryError(OPTED_OUT, "OptedOut", ""));
            String uri = url + "?" + addParameters(request).toQuery();
            DefaultHttpClient client = createClient(tracking.getUserAgent());
            HttpResponse response = client.execute(new HttpGet(uri));
            HttpEntity entity = response.getEntity();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try {
                entity.writeTo(bout);
            } catch (SocketException e) {
                // can happen due to Connection Reset, but still return a valid response
                Logging.error(getClass(), "Exception writing output ", e);
            }
            Logging.debug(getClass(), "Received response " + bout.toString("UTF-8"));
            return new TapestryResponse(bout.toString("UTF-8"));
        } catch (Exception e) {
            Logging.error(getClass(), "Exception sending request ", e);
            return new TapestryResponse(new TapestryError(CLIENT_REQUEST_ERROR, "ClientRequestError", "Exception: " + e));
        }
    }

    /**
     * Opts this device into tracking.  Tapestry will collect ids and send requests from this app.
     */
    public void optIn() {
        tracking.getPreferences().edit().remove(PREF_TAPAD_DEVICE_ID).commit();
    }

    /**
     * Opts this device out of all tracking. Tapestry will not be able to send requests from this app.  All responses
     * will be a {@link TapestryResponse} containing an {@link TapestryError#OPTED_OUT} error.
     */
    public void optOut() {
        tracking.getPreferences().edit().putString(PREF_TAPAD_DEVICE_ID, OPTED_OUT_DEVICE_ID).commit();
    }

    /**
     * Adds parameters which are required (e.g. device identifiers and partner id) to a request.
     *
     * @param request A request
     * @return the request with additional parameters
     */
    public TapestryRequest addParameters(TapestryRequest request) {
        for (TypedIdentifier identifier : tracking.getIds())
            request.typedDid(identifier.getType(), identifier.getValue());
        return request.partnerId(partnerId).get();
    }

    private DefaultHttpClient createClient(String userAgent) {
        // Event occur infrequently, so we use a vanilla single-threaded client.
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUserAgent(params, userAgent);
        DefaultHttpClient client = new DefaultHttpClient(params);
        // Keep connections alive for 5 seconds.
        client.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                return 5000;
            }
        });
        return client;
    }
}