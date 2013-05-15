package com.tapad.tapestry;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class TapestryRequest {
    private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

    public TapestryRequest setData(String key, String value) {
        return addMapParameter("ta_set_data", key, value);
    }

    public TapestryRequest addData(String key, String value) {
        return addMapParameter("ta_add_data", key, value);
    }

    public TapestryRequest addAudiences(String... audiences) {
        return addArrayParameter("ta_add_audiences", audiences);
    }

    public TapestryRequest listDevices() {
        return addParameter("ta_list_devices", "");
    }

    public TapestryRequest strength(int i) {
        return addParameter("ta_strength", i + "");
    }

    public TapestryRequest depth(int i) {
        return addParameter("ta_depth", i + "");
    }

    public TapestryRequest userIds(String key, String value) {
        return addMapParameter("ta_user_ids", key, value);
    }

    protected TapestryRequest partnerId(String id) {
        return addParameter("ta_partner_id", id);
    }

    protected TapestryRequest typedDid(String key, String value) {
        return addMapParameter("ta_typed_did", key, value);
    }

    public TapestryRequest getIds(String... keys) {
        return addArrayParameter("ta_get_ids", keys);
    }

    public TapestryRequest getPlatforms() {
        return addParameter("ta_get_platforms", "");
    }

    public TapestryRequest getAudiences() {
        return addParameter("ta_get_audiences", "");
    }

    public TapestryRequest getData(String... keys) {
        return addArrayParameter("ta_get_data", keys);
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

    public TapestryRequest addParameter(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

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

    public Map<String, Object> getParameters() {
        return parameters;
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
