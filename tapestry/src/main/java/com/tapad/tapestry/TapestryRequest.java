package com.tapad.tapestry;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * A mutable class for building requests that are sent with {@link TapestryClient}.  Building a request adds parameters
 * to the HTTP query string that will be sent to Tapestry Web API.
 * <p/>
 * An example of building a request:
 * <blockquote><pre>
 * TapestryRequest request = new TapestryRequest()
 *     .addAudiences("aud1", "aud2", "aud3")
 *     .addAudiences("aud4")
 *     .addData("color", "blue")
 *     .addData("model", "ford")
 *     .listDevices()
 *     .depth(2);
 * </pre></blockquote>
 */
public class TapestryRequest {
    private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

    /**
     * Sets the value for a data key.
     *
     * @param key   The data key
     * @param value The value to set the key to
     */
    public TapestryRequest setData(String key, String value) {
        return addMapParameter("ta_set_data", key, value);
    }

    /**
     * Adds a value to a data key.  Only one value can be added per request, so calling this method with the same key
     * more than once will simply replace any existing values with the most recent.
     *
     * @param key   The data key
     * @param value The value to add to the key
     */
    public TapestryRequest addData(String key, String value) {
        return addMapParameter("ta_add_data", key, value);
    }

    /**
     * Adds this device to one or more audiences.
     *
     * @param audiences A list of audiences
     */
    public TapestryRequest addAudiences(String... audiences) {
        return addArrayParameter("ta_add_audiences", audiences);
    }

    /**
     * Tells Tapestry to return all the devices listed out as well as combined.  The list of devices can be accessed in
     * {@link com.tapad.tapestry.TapestryResponse#getDevices()}
     */
    public TapestryRequest listDevices() {
        return addParameter("ta_list_devices", "");
    }

    /**
     * @param strength The strength value between 1-5 inclusive. Default is 2
     */
    public TapestryRequest strength(int strength) {
        return addParameter("ta_strength", strength + "");
    }

    /**
     * @param depth The depth value between 0-2 inclusive.  Default is 1
     */
    public TapestryRequest depth(int depth) {
        return addParameter("ta_depth", depth + "");
    }

    /**
     * Sets the cross-device user id
     *
     * @param type The type of user id, e.g. "google email address"
     * @param id   The id itself, e.g. "user@gmail.com"
     */
    public TapestryRequest userIds(String type, String id) {
        return addMapParameter("ta_user_ids", type, id);
    }

    protected TapestryRequest partnerId(String id) {
        return addParameter("ta_partner_id", id);
    }

    protected TapestryRequest typedDid(String key, String value) {
        return addMapParameter("ta_typed_did", key, value);
    }

    protected TapestryRequest get() {
        return addParameter("ta_get", "");
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
                params.add(new BasicNameValuePair(key, new JSONArray((List) value).toString()));
            else if (value instanceof Map)
                params.add(new BasicNameValuePair(key, new JSONObject((Map) value).toString()));
        }
        return URLEncodedUtils.format(params, "UTF-8");
    }

    private TapestryRequest addParameter(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    private TapestryRequest addMapParameter(String name, String key, String value) {
        Object param = parameters.get(name);
        Map<String, String> map = new HashMap<String, String>();
        if (param != null && param instanceof Map)
            map = ((Map) param);
        map.put(key, value);
        return addParameter(name, map);
    }

    private TapestryRequest addArrayParameter(String name, String... values) {
        Object param = parameters.get(name);
        List<String> list = new ArrayList<String>();
        if (param != null && param instanceof List)
            list = ((List) param);
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
