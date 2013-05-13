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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TapestryClient {
    public final String url;
    public final DefaultHttpClient client;
    private final TapestryTracking tracking;
    private final String partnerId;
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    public TapestryClient(Context context, String partnerId) {
        this(new TapestryTracking(context), partnerId, "http://tapestry.tapad.com/tapestry/1");
    }

    public TapestryClient(TapestryTracking tracking, String partnerId, String url) {
        this.tracking = tracking;
        this.partnerId = partnerId;
        this.url = url;
        this.client = createClient(tracking.getUserAgent());
    }

    public void send(final TapestryRequest request, final TapestryCallback callback) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                callback.receive(send(request));
            }
        });
    }

    public TapestryResponse send(TapestryRequest request) {
        try {
            String uri = url + "?" + addParameters(request).toQuery();
            HttpResponse response = client.execute(new HttpGet(uri));
            HttpEntity entity = response.getEntity();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            entity.writeTo(bout);
            return new TapestryResponse(bout.toString("UTF-8"));
        } catch (Exception e) {
            Logging.error("TapestryClient", "Exception sending request " + e.getMessage());
            return new TapestryResponse("{errors:['Exception sending request']}");
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