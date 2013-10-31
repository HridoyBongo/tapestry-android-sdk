package com.tapad.tapestry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A mutable class for building requests that are sent with {@link TapestryClient}. Building a request adds parameters
 * to the HTTP query string that will be sent to Tapestry Web API.
 * <p/>
 * An example of building a request: <blockquote>
 * 
 * <pre>
 * TapestryRequest request = new TapestryRequest().addAudiences(&quot;aud1&quot;, &quot;aud2&quot;, &quot;aud3&quot;).addAudiences(&quot;aud4&quot;).addData(&quot;color&quot;, &quot;blue&quot;).addData(&quot;model&quot;, &quot;ford&quot;).listDevices().depth(2);
 * </pre>
 * 
 * </blockquote>
 */
public class TapestryRequest {
	private final Map<String, Object> parameters = new LinkedHashMap<String, Object>();

	/**
	 * Clear data removes all data from one or more keys.
	 * 
	 * @param keys
	 *            The list of keys
	 */
	public TapestryRequest clearData(String... keys) {
		return addArrayParameter("ta_clear_data", keys);
	}

	/**
	 * Will add a value to a key without duplicates
	 * 
	 * @param key
	 *            The data key
	 * @param value
	 *            The value to set-add to the key
	 */
	public TapestryRequest addUniqueData(String key, String value) {
		return addMapParameter("ta_sadd_data", key, value);
	}

	/**
	 * Removes the value for a data key.
	 * 
	 * @param key
	 *            The data key
	 * @param value
	 *            The value to remove from the key
	 */
	public TapestryRequest removeData(String key, String value) {
		return addMapParameter("ta_remove_data", key, value);
	}

	/**
	 * Sets the value for a data key.
	 * 
	 * @param key
	 *            The data key
	 * @param value
	 *            The value to set the key to
	 */
	public TapestryRequest setData(String key, String value) {
		return addMapParameter("ta_set_data", key, value);
	}

	/**
	 * Adds a value to a data key. Only one value can be added per request, so calling this method with the same key
	 * more than once will simply replace any existing values with the most recent.
	 * 
	 * @param key
	 *            The data key
	 * @param value
	 *            The value to add to the key
	 */
	public TapestryRequest addData(String key, String value) {
		return addMapParameter("ta_add_data", key, value);
	}

	/**
	 * Adds this device to one or more audiences.
	 * 
	 * @param audiences
	 *            A list of audiences
	 */
	public TapestryRequest addAudiences(String... audiences) {
		return addArrayParameter("ta_add_audiences", audiences);
	}

	/**
	 * Removes this device to one or more audiences.
	 * 
	 * @param audiences
	 *            A list of audiences
	 */
	public TapestryRequest removeAudiences(String... audiences) {
		return addArrayParameter("ta_remove_audiences", audiences);
	}

	/**
	 * Tells Tapestry to return all the devices listed out as well as combined. The list of devices can be accessed in
	 * {@link com.tapad.tapestry.TapestryResponse#getDevices()}
	 */
	public TapestryRequest listDevices() {
		return addParameter("ta_list_devices", "");
	}

	/**
	 * @param strength
	 *            The strength value between 1-5 inclusive. Default is 2
	 */
	public TapestryRequest strength(int strength) {
		return addParameter("ta_strength", strength + "");
	}

	/**
	 * Tells Tapestry to consider devices this many steps away from the source device.
	 * 
	 * @param depth
	 *            The depth value between 0-2 inclusive. Default is 1
	 */
	public TapestryRequest depth(int depth) {
		return addParameter("ta_depth", depth + "");
	}

	/**
	 * Sets the cross-device user id
	 * 
	 * @param type
	 *            The type of user id, e.g. "google email address"
	 * @param id
	 *            The id itself, e.g. "user@gmail.com"
	 */
	public TapestryRequest userIds(String type, String id) {
		return addMapParameter("ta_user_ids", type, id);
	}

	/**
	 * Sets the analytics parameter for tracking in analytics platforms
	 * 
	 * @param isNewSession
	 *            Has enough time elapsed to be considered a new tracking session
	 */
	public TapestryRequest analytics(Boolean isNewSession) {
		return addMapParameter("ta_analytics", "isNewSession", isNewSession.toString());
	}

	/**
	 * Add a typed device id, e.g. OpenUDID or IDFA. The client sets these automatically; under normal circumstances it is not necessary to set your own.
	 * 
	 * @param type - type of the identifier
	 * @param id - the id
	 */
	public TapestryRequest typedDid(String type, String id) {
		return addMapParameter("ta_typed_did", type, id);
	}

	/**
	 * Set a partner device id, typically the mobile browser's cookie id.
	 * 
	 * @param id - the id value
	 */
	public TapestryRequest partnerDeviceId(String id) {
		return addParameter("ta_partner_did", id);
	}

	protected TapestryRequest get() {
		return addParameter("ta_get", "");
	}

	protected TapestryRequest platform(String platform) {
		return addParameter("ta_platform", platform);
	}

	protected TapestryRequest partnerId(String id) {
		return addParameter("ta_partner_id", id);
	}

	/**
	 * Converts the request into a URL-encoded query string.
	 * 
	 * @return query string
	 */
	public String toQuery() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			Object value = entry.getValue();
			String key = entry.getKey();
			if (value instanceof String)
				params.add(new BasicNameValuePair(key, (String) value));
			else if (value instanceof List)
				params.add(new BasicNameValuePair(key, new JSONArray((List<?>) value).toString()));
			else if (value instanceof Map)
				params.add(new BasicNameValuePair(key, new JSONObject((Map<?, ?>) value).toString()));
		}
		return URLEncodedUtils.format(params, "UTF-8");
	}

	private TapestryRequest addParameter(String name, Object value) {
		parameters.put(name, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	private TapestryRequest addMapParameter(String name, String key, String value) {
		Object param = parameters.get(name);
		Map<String, String> map = new HashMap<String, String>();
		if (param != null && param instanceof Map)
			map = ((Map<String, String>) param);
		map.put(key, value);
		return addParameter(name, map);
	}

	@SuppressWarnings("unchecked")
	private TapestryRequest addArrayParameter(String name, String... values) {
		Object param = parameters.get(name);
		List<String> list = new ArrayList<String>();
		if (param != null && param instanceof List)
			list = ((List<String>) param);
		list.addAll(Arrays.asList(values));
		return addParameter(name, list);
	}

	@Override
	public String toString() {
		return parameters.toString();
	}

	@Override
	public boolean equals(Object o) {
		return toString().equals(o.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
