package com.tapad.tapestry;

import android.test.AndroidTestCase;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tapad.tracking.deviceidentification.TypedIdentifier.TYPE_ANDROID_ID_MD5;

public class TapestryClientTest extends AndroidTestCase {
    public static final String TEST_URL = "http://tapestry.tapad.com/tapestry/1";
    public static final String HTTPS_TEST_URL = "https://tapestry.tapad.com/tapestry/1";
    public static final String TEST_PARTNER_ID = "1";
    private TapestryTracking tracking = null;
    
    @Override
    protected void setUp() throws Exception {
        tracking = new TapestryTracking(getContext());
    }

    public void test_can_connect_over_https() {
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, HTTPS_TEST_URL);
        TapestryResponse response = client.sendSynchronously(new TapestryRequest().setData("color", "blue"));
        assertEquals(response.getData("color").get(0), "blue");
    }

    public void test_returns_opt_out_if_opted_out() {
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, TEST_URL);
        client.optOut(getContext());
        TapestryResponse response = client.sendSynchronously(new TapestryRequest());
        assertEquals(response.getErrors().get(0).getType(), TapestryError.OPTED_OUT);
        client.optIn(getContext());
    }

    public void test_should_send_requests_and_receive_responses() {
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, TEST_URL);
        TapestryResponse response = client.sendSynchronously(new TapestryRequest().setData("color", "blue"));
        assertEquals(response.getData("color").get(0), "blue");
    }

    public void test_should_send_asynchronous_requests() throws Exception {
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, TEST_URL);
        final AtomicBoolean called = new AtomicBoolean();
        client.send(new TapestryRequest(), new TapestryCallback() {
            @Override
            public void receive(TapestryResponse response) {
                called.set(true);
            }
        });
        Thread.sleep(1000);
        assertTrue(called.get());
    }

    public void test_should_return_errors_in_response() {
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, "bad url");
        TapestryResponse response = client.sendSynchronously(new TapestryRequest());
        assertEquals(response.getErrors().get(0).getType(), TapestryError.CLIENT_REQUEST_ERROR);
    }
}