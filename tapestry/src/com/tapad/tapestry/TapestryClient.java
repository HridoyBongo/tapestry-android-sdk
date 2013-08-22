package com.tapad.tapestry;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.tapad.tapestry.TapestryError.CLIENT_REQUEST_ERROR;
import static com.tapad.tapestry.TapestryError.OPTED_OUT;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tapad.tapestry.deviceidentification.TypedIdentifier;
import com.tapad.tapestry.http.HttpStack;
import com.tapad.tapestry.http.HttpStackFactory;

/**
 * A client for sending requests to the Tapestry Web API and returning responses. The client should be instantiated once
 * your activity has entered {@code onCreate()}. If you use the client across many Activities, you may wish to use the
 * {@link TapestryService} wrapper instead.
 * <p/>
 * The client sends a {@link TapestryRequest} as an HTTP request to the Tapestry Web API and parses the JSON response as
 * {@link TapestryResponse}. When calling the client from the Android UI thread, it is recommended to send requests
 * asynchronously by passing in a {@link TapestryCallback} so as not to block the UI thread.
 * <p/>
 * An example of sending a request and receiving an asynchronous response: <blockquote>
 * 
 * <pre>
 * TapestryClient client = new TapestryClient(context);
 * TapestryRequest request = new TapestryRequest();
 * // TODO build request
 * client.send(request, new TapestryCallback() {
 *     {@literal @}Override
 *     public void receive(TapestryResponse response) {
 *         // TODO handle response
 *     }
 * });
 * </pre>
 * 
 * </blockquote> Note if the callback updates the UI then you should use the {@link TapestryUICallback} convenience
 * class which runs the callback on the UI thread. The client throws no exceptions unless
 * {@link Logging#setThrowExceptions(boolean)} has been set to true.
 */
public class TapestryClient {
	public static final String DEFAULT_URL = "http://tapestry.tapad.com/tapestry/1";
	public static final String PREF_TAPAD_DEVICE_ID = "_tapad_device_id";
	public static final String OPTED_OUT_DEVICE_ID = "OptedOut";
	public static final String TAPESTRY_HEADER = "X-Tapestry-Id";
	private final ConnectionStatusExecutor connectionExecutor;
	private final TapestryTracking tracking;
	private final String url;
	private final String partnerId;
	private final HttpStack stack;

	/**
	 * Creates client ready to receive requests. Client cannot be instantiated before {@code onCreate} is called.
	 * Partner id will be read from {@code tapad.PARTNER_ID} in the manifest or default if none exists. The url of the
	 * API will be read from {@code tapad.API_URL} in the manifest or default if none exists.
	 * 
	 * @param context
	 *            The context of the app
	 */
	public TapestryClient(Context context) {
		this(context, getMetaData(context, "tapad.PARTNER_ID", null));
	}

	/**
	 * Creates client ready to receive requests. Client cannot be instantiated before {@code onCreate} is called. The
	 * url of the API will be read from {@code tapad.API_URL} in the manifest or default if none exists.
	 * 
	 * @param context
	 *            The context of the app
	 * @param partnerId
	 *            The Tapestry partner id that has been assigned to you
	 */
	public TapestryClient(Context context, String partnerId) {
		this(context, partnerId, getMetaData(context, "tapad.API_URL", DEFAULT_URL));
	}

	/**
	 * Creates client ready to receive requests. Client cannot be instantiated before {@code onCreate} is called.
	 * 
	 * @param context
	 *            The context of the app
	 * @param partnerId
	 *            The Tapestry partner id that has been assigned to you
	 * @param url
	 *            The url of the tapestry host
	 */
	public TapestryClient(Context context, String partnerId, String url) {
		this(context, partnerId, url, HttpStackFactory.getDefaultStack(context));
	}

	/**
	 * Creates client ready to receive requests. Client cannot be instantiated before {@code onCreate} is called.
	 * 
	 * @param context
	 *            The context of the app
	 * @param partnerId
	 *            The Tapestry partner id that has been assigned to you
	 * @param url
	 *            The url of the tapestry host
	 * @param stack
	 *            The stack for sending and receiving http requests
	 */
	public TapestryClient(Context context, String partnerId, String url, HttpStack stack) {
		this.tracking = new TapestryTracking(context);
		this.partnerId = partnerId;
		this.url = url;
		this.stack = stack;
		this.connectionExecutor = new ConnectionStatusExecutor(context);

		if (this.partnerId == null)
			throw new RuntimeException("Partner id must be specified in the manifest or during instantiation");
	}

	private static String getMetaData(Context context, String key, String defaultValue) {
		try {
			String value = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.get(key).toString();
			return value == null ? defaultValue : value;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Sends a request asynchronously using a worker thread pool.
	 * 
	 * @param callback
	 *            A callback that will be called when the Tapestry server responds
	 */
	public void send(final TapestryCallback callback) {
		send(new TapestryRequest(), callback);
	}

	/**
	 * Sends a request asynchronously using a worker thread pool, without returning a response.
	 * 
	 * @param request
	 *            The request
	 */
	public void send(final TapestryRequest request) {
		send(request, TapestryCallback.DO_NOTHING);
	}

	/**
	 * Sends a request asynchronously using a worker thread pool.
	 * 
	 * @param request
	 *            The request
	 * @param callback
	 *            A callback that will be called when the Tapestry server responds
	 */
	public void send(final TapestryRequest request, final TapestryCallback callback) {
		final long timeOfRequest = System.currentTimeMillis();
		connectionExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					TapestryResponse response = sendSynchronously(request);
					callback.receive(response, null, System.currentTimeMillis() - timeOfRequest);
				} catch (Exception e) {
					Logging.e("Exception sending request ", e);
					TapestryResponse response = new TapestryResponse(new TapestryError(CLIENT_REQUEST_ERROR, "ClientRequestError", "Exception: " + e));
					callback.receive(response, e, System.currentTimeMillis() - timeOfRequest);
				}
			}
		});
	}

	/**
	 * Sends a request synchronously, blocking until response is received.
	 * 
	 * @param request
	 *            The request
	 * @return a response from the server
	 */
	public TapestryResponse sendSynchronously(TapestryRequest request) throws Exception {
		if (tracking.getDeviceId().equals(OPTED_OUT_DEVICE_ID))
			return new TapestryResponse(new TapestryError(OPTED_OUT, "OptedOut", ""));
		String uri = url + "?" + addParameters(request).toQuery();
		String response = stack.performGet(uri, TAPESTRY_HEADER, partnerId);
		Logging.d("Received response " + response);
		return new TapestryResponse(response);
	}

	/**
	 * Opts this device into tracking. Tapestry will collect ids and send requests from this app.
	 */
	public void optIn(Context context) {
		getDefaultSharedPreferences(context).edit().remove(PREF_TAPAD_DEVICE_ID).commit();
		tracking.updateDeviceId(context);
	}

	/**
	 * Opts this device out of all tracking. Tapestry will not be able to send requests from this app. All responses
	 * will be a {@link TapestryResponse} containing an {@link TapestryError#OPTED_OUT} error.
	 */
	public void optOut(Context context) {
		getDefaultSharedPreferences(context).edit().putString(PREF_TAPAD_DEVICE_ID, OPTED_OUT_DEVICE_ID).commit();
		tracking.updateDeviceId(context);
	}

	/**
	 * Adds parameters which are required (e.g. device identifiers and partner id) to a request.
	 * 
	 * @param request
	 *            A request
	 * @return the request with additional parameters
	 */
	public TapestryRequest addParameters(TapestryRequest request) {
		for (TypedIdentifier identifier : tracking.getIds())
			request.typedDid(identifier.getType(), identifier.getValue());
		if (!TextUtils.isEmpty(tracking.getPlatform()))
			request.platform(tracking.getPlatform());
		return request.partnerId(partnerId).get();
	}
}