package com.tapad.tapestry;

import android.content.Context;
import android.content.pm.PackageManager;
import com.tapad.tracking.deviceidentification.TypedIdentifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.ByteArrayOutputStream;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.tapad.tapestry.TapestryError.CLIENT_REQUEST_ERROR;
import static com.tapad.tapestry.TapestryError.OPTED_OUT;

/**
 * A client for sending requests to the Tapestry Web API and returning responses.  The client should be instantiated
 * once your activity has entered {@code onCreate()}.  If you use the client across many Activities, you may wish to use
 * the {@link TapestryService} wrapper instead.
 * <p/>
 * The client sends a {@link TapestryRequest} as an HTTP request to the Tapestry Web API and parses the JSON response as
 * {@link TapestryResponse}.  When calling the client from the Android UI thread, it is recommended to send requests
 * asynchronously by passing in a {@link TapestryCallback} so as not to block the UI thread.
 * <p/>
 * An example of sending a request and receiving an asynchronous response:
 * <blockquote><pre>
 * TapestryClient client = new TapestryClient(context);
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
    public static final String DEFAULT_URL = "http://tapestry.tapad.com/tapestry/1";
    public static final String PREF_TAPAD_DEVICE_ID = "_tapad_device_id";
    public static final String OPTED_OUT_DEVICE_ID = "OptedOut";
    private static final int SOCKET_OPERATION_TIMEOUT = 10 * 1000;
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final TapestryTracking tracking;
    private final String url;
    private final String partnerId;
    private final DefaultHttpClient client;

    /**
     * Creates client ready to receive requests.  Client cannot be instantiated before {@code onCreate} is called.
     * Partner id will be read from {@code tapad.PARTNER_ID} in the manifest or default if none exists.  The url of the
     * API will be read from {@code tapad.API_URL} in the manifest or default if none exists.
     *
     * @param context The context of the app
     */
    public TapestryClient(Context context) {
        this(context, getMetaData(context, "tapad.PARTNER_ID", null));
    }

    /**
     * Creates client ready to receive requests.  Client cannot be instantiated before {@code onCreate} is called.  The
     * url of the API will be read from {@code tapad.API_URL} in the manifest or default if none exists.
     *
     * @param context   The context of the app
     * @param partnerId The Tapestry partner id that has been assigned to you
     */
    public TapestryClient(Context context, String partnerId) {
        this(context, partnerId, getMetaData(context, "tapad.API_URL", DEFAULT_URL));
    }

    /**
     * Creates client ready to receive requests.  Client cannot be instantiated before {@code onCreate} is called.
     *
     * @param context   The context of the app
     * @param partnerId The Tapestry partner id that has been assigned to you
     * @param url       The url of the tapestry host
     */
    public TapestryClient(Context context, String partnerId, String url) {
        this(new TapestryTracking(context), partnerId, url);
    }

    private static String getMetaData(Context context, String key, String defaultValue) {
        try {
            String value = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.get(key).toString();
            return value == null ? defaultValue : value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected TapestryClient(TapestryTracking tracking, String partnerId, String url) {
        this.tracking = tracking;
        this.partnerId = partnerId;
        this.url = url;
        if (this.partnerId == null)
            throw new RuntimeException("Partner id must be specified in the manifest or during instantiation");
        client = createClient(tracking.getUserAgent());
    }

    /**
     * Sends a request asynchronously using a worker thread pool, without returning a response.
     *
     * @param request The request
     */
    public void send(final TapestryRequest request) {
        send(request, TapestryCallback.DO_NOTHING);
    }

    /**
     * Sends a request asynchronously using a worker thread pool.
     *
     * @param request  The request
     * @param callback A callback that will be called when the Tapestry server responds
     */
    public void send(final TapestryRequest request, final TapestryCallback callback) {
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
            if (tracking.getDeviceId().equals(OPTED_OUT_DEVICE_ID))
                return new TapestryResponse(new TapestryError(OPTED_OUT, "OptedOut", ""));
            String uri = url + "?" + addParameters(request).toQuery();
            HttpGet http = new HttpGet(uri);
            http.setHeader("X-Tapestry-Id", partnerId);
            HttpResponse response = client.execute(http);
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
    public void optIn(Context context) {
        getDefaultSharedPreferences(context).edit().remove(PREF_TAPAD_DEVICE_ID).commit();
        tracking.updateDeviceId(context);
    }

    /**
     * Opts this device out of all tracking. Tapestry will not be able to send requests from this app.  All responses
     * will be a {@link TapestryResponse} containing an {@link TapestryError#OPTED_OUT} error.
     */
    public void optOut(Context context) {
        getDefaultSharedPreferences(context).edit().putString(PREF_TAPAD_DEVICE_ID, OPTED_OUT_DEVICE_ID).commit();
        tracking.updateDeviceId(context);
    }

    /**
     * Adds parameters which are required (e.g. device identifiers and partner id) to a request.
     *
     * @param request A request
     * @return the request with additional parameters
     */
    protected TapestryRequest addParameters(TapestryRequest request) {
        for (TypedIdentifier identifier : tracking.getIds())
            request.typedDid(identifier.getType(), identifier.getValue());
        if (!tracking.getPlatform().isEmpty())
            request.platform(tracking.getPlatform());
        return request.partnerId(partnerId).get();
    }

    /**
     * Creates a thread-safe http client with settings based on the {@link android.net.http.AndroidHttpClient}.  We do
     * not depend on the AndroidHttpClient in order to be compatible with older Android SDKs.
     *
     * @param userAgent The user agent to use
     * @return an http client
     */
    protected DefaultHttpClient createClient(String userAgent) {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUserAgent(params, userAgent);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpClientParams.setRedirecting(params, false);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
        DefaultHttpClient client = new DefaultHttpClient(manager, params);
        return client;
    }
}