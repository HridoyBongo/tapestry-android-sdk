package com.tapad.tapestry;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

public class TapestryClientTest extends AndroidTestCase {
	public static final String TEST_URL = "http://tapestry.tapad.com/tapestry/1";
	public static final String HTTPS_TEST_URL = "https://tapestry.tapad.com/tapestry/1";
	public static final String TEST_PARTNER_ID = "1";

	public void test_can_connect_over_https() throws Exception {
		TapestryClient client = new TapestryClient(getContext(), TEST_PARTNER_ID, HTTPS_TEST_URL);
		TapestryResponse response = client.sendSynchronously(new TapestryRequest().setData("color", "blue"));
		assertEquals(response.getData("color").get(0), "blue");
	}

	public void test_returns_opt_out_if_opted_out() throws Exception {
		TapestryClient client = new TapestryClient(getContext(), TEST_PARTNER_ID, TEST_URL);
		client.optOut(getContext());
		TapestryResponse response = client.sendSynchronously(new TapestryRequest());
		assertEquals(response.getErrors().get(0).getType(), TapestryError.OPTED_OUT);
		client.optIn(getContext());
	}

	public void test_should_send_requests_and_receive_responses() throws Exception {
		TapestryClient client = new TapestryClient(getContext(), TEST_PARTNER_ID, TEST_URL);
		TapestryResponse response = client.sendSynchronously(new TapestryRequest().setData("color", "blue"));
		assertEquals(response.getData("color").get(0), "blue");
	}

	public void test_should_return_errors_in_response() throws Exception {
		TapestryClient client = new TapestryClient(getContext(), TEST_PARTNER_ID, "bad url");
		final List<TapestryResponse> responseHolder = new ArrayList<TapestryResponse>();
		final List<Exception> exceptionHolder = new ArrayList<Exception>();
		client.send(new TapestryRequest(), new TapestryCallback() {
			@Override
			public void receive(TapestryResponse response, Exception e, long millisSinceInvocation) {
				responseHolder.add(response);
				exceptionHolder.add(e);
			}
		});
		Thread.sleep(1000);
		assertTrue(exceptionHolder.size() == 1);
		assertEquals(responseHolder.get(0).getErrors().get(0).getType(), TapestryError.CLIENT_REQUEST_ERROR);
	}
}