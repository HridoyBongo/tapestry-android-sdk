package com.tapad.tapestry;

import com.tapad.tracking.deviceidentification.TypedIdentifier;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tapad.tracking.deviceidentification.TypedIdentifier.TYPE_ANDROID_ID_MD5;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class TapestryClientTest {
    public static final String TEST_URL = "http://tapestry.tapad.com/tapestry/1";
    public static final String HTTPS_TEST_URL = "https://tapestry.tapad.com/tapestry/1";
    public static final String TEST_PARTNER_ID = "1";
    private TapestryTracking tracking = mock(TapestryTracking.class);

    @Before
    public void setup() {
        when(tracking.getIds()).thenReturn(new ArrayList<TypedIdentifier>());
        when(tracking.getDeviceId()).thenReturn("");
    }

    @Test
    public void can_connect_over_https() {
        when(tracking.getIds()).thenReturn(Arrays.asList(new TypedIdentifier(TYPE_ANDROID_ID_MD5, "111")));
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, HTTPS_TEST_URL);
        TapestryResponse response = client.sendSynchronously(new TapestryRequest().setData("color", "blue"));
        assertThat(response.getIds(TYPE_ANDROID_ID_MD5), hasItem("111"));
        assertThat(response.getData("color"), hasItem("blue"));    }

    @Test
    public void returns_opt_out_if_opted_out() {
        when(tracking.getDeviceId()).thenReturn(TapestryClient.OPTED_OUT_DEVICE_ID);
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, TEST_URL);
        TapestryResponse response = client.sendSynchronously(new TapestryRequest());
        Logging.setEnabled(true);
        assertThat(response.getErrors().get(0).getType(), equalTo(TapestryError.OPTED_OUT));
    }

    @Test
    public void should_send_requests_and_receive_responses() {
        when(tracking.getIds()).thenReturn(Arrays.asList(new TypedIdentifier(TYPE_ANDROID_ID_MD5, "111")));
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, TEST_URL);
        TapestryResponse response = client.sendSynchronously(new TapestryRequest().setData("color", "blue"));
        assertThat(response.getIds(TYPE_ANDROID_ID_MD5), hasItem("111"));
        assertThat(response.getData("color"), hasItem("blue"));
    }

    @Test
    public void should_send_asynchronous_requests() throws Exception {
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

    @Test
    public void should_return_errors_in_response() {
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, "bad url");
        TapestryResponse response = client.sendSynchronously(new TapestryRequest());
        assertThat(response.getErrors().get(0).getType(), equalTo(TapestryError.CLIENT_REQUEST_ERROR));
    }

    @Test
    public void should_decorate_requests_with_partner_id_and_tracked_ids() throws Exception {
        when(tracking.getIds()).thenReturn(Arrays.asList(new TypedIdentifier("sometype", "someval")));
        TapestryClient client = new TapestryClient(tracking, TEST_PARTNER_ID, "");
        assertEquals(client.addParameters(new TapestryRequest()), new TapestryRequest().typedDid("sometype", "someval").partnerId(TEST_PARTNER_ID).get());
    }
}