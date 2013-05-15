package com.tapad.tapestry;

import android.app.Activity;
import android.content.Context;
import com.tapad.tracking.deviceidentification.TypedIdentifier;
import com.tapad.util.Logging;
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

public class TapestryClient {
    private final TapestryTracking tracking;
    private final String url;
    private final String partnerId;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public TapestryClient(Context context, String partnerId) {
        this(new TapestryTracking(context), partnerId, "http://tapestry.tapad.com/tapestry/1");
    }

    public TapestryClient(TapestryTracking tracking, String partnerId, String url) {
        this.tracking = tracking;
        this.partnerId = partnerId;
        this.url = url;
    }

    /**
     * Opts the device back in after an opt out.
     */
    public void optIn() {
        tracking.getPreferences().edit().remove(PREF_TAPAD_DEVICE_ID).commit();
    }

    /**
     * Opts the device out of all tracking / personalization by setting the device id to the constant
     * string OptedOut. This means that it is now impossible to distinguish this device from all
     * other opted out device.
     */
    public void optOut() {
        tracking.getPreferences().edit().putString(PREF_TAPAD_DEVICE_ID, OPTED_OUT_DEVICE_ID).commit();
    }

    public void send(final Activity activity, final TapestryRequest request, final TapestryCallback callback) {
        send(request, new TapestryCallback() {
            @Override
            public void receive(final TapestryResponse response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.receive(response);
                    }
                });
            }
        });
    }

    public void send(final TapestryRequest request, final TapestryCallback callback) {
        tracking.getUserAgent();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                callback.receive(send(request));
            }
        });
    }

    public TapestryResponse send(TapestryRequest request) {
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

    public TapestryRequest addParameters(TapestryRequest request) {
        for (TypedIdentifier identifier : tracking.getIds())
            request.typedDid(identifier.getType(), identifier.getValue());
        return request.partnerId(partnerId);
    }

    public static DefaultHttpClient createClient(String userAgent) {
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