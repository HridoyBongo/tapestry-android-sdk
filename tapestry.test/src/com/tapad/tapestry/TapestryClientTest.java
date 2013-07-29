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
    	super.setUp();
        tracking = new TapestryTracking(getContext());
    }
    

    public void test_can_connect_over_https() {
        //when(tracking.getIds()).thenReturn(Arrays.asList(new TypedIdentifier(TYPE_ANDROID_ID_MD5, "111")));
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, HTTPS_TEST_URL);
        TapestryResponse response = client.sendSynchronously(new TapestryRequest().setData("color", "blue"));
        assertEquals(response.getIds(TYPE_ANDROID_ID_MD5).get(0), "111");
        assertEquals(response.getData("color").get(0), "blue");
    }

    public void test_returns_opt_out_if_opted_out() {
        // when(tracking.getDeviceId()).thenReturn(TapestryClient.OPTED_OUT_DEVICE_ID);
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, TEST_URL);
        TapestryResponse response = client.sendSynchronously(new TapestryRequest());
        assertEquals(response.getErrors().get(0).getType(), TapestryError.OPTED_OUT);
    }

    public void test_should_send_requests_and_receive_responses() {
        // when(tracking.getIds()).thenReturn(Arrays.asList(new TypedIdentifier(TYPE_ANDROID_ID_MD5, "111")));
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, TEST_URL);
        TapestryResponse response = client.sendSynchronously(new TapestryRequest().setData("color", "blue"));
        assertEquals(response.getIds(TYPE_ANDROID_ID_MD5).get(0), "111");
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

    public void test_should_decorate_requests_with_partner_id_and_tracked_ids() throws Exception {
        // when(tracking.getIds()).thenReturn(Arrays.asList(new TypedIdentifier("sometype", "someval")));
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, "");
        assertEquals(client.addParameters(new TapestryRequest()), new TapestryRequest().typedDid("sometype", "someval").partnerId(TEST_PARTNER_ID).get());
    }

    public void test_should_decorate_requests_with_platform_when_defined() {
        // when(tracking.getPlatform()).thenReturn("platform");
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, "");
        assertEquals(client.addParameters(new TapestryRequest()), new TapestryRequest().platform("platform").partnerId(TEST_PARTNER_ID).get());
    }
}