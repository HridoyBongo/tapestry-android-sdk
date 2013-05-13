package com.tapad.tapestry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TapestryResponse {
    public JSONObject json;

    public TapestryResponse(String response) {
        try {
            json = new JSONObject(response);
        } catch (JSONException e) {
            Logging.warn("TapestryResponse", "Could not parse " + response);
            json = new JSONObject();
        }
    }

    public List<TapestryResponse> getDevices() {
        ArrayList<TapestryResponse> devices = new ArrayList<TapestryResponse>();
        for (String device : getList("devices")) {
            devices.add(new TapestryResponse(device));
        }
        return devices;
    }

    public List<String> getPlatforms() {
        return getList("platforms");
    }

    public List<String> getErrors() {
        return getList("errors");
    }

    public List<String> getAudiences() {
        return getList("audiences");
    }

    public Map<String, List<String>> getData() {
        return getStringListMap("data");
    }

    public Map<String, List<String>> getIds() {
        return getStringListMap("ids");
    }

    public List<String> getList(String key) {
        try {
            return jsonArrayToStringList(json.getJSONArray(key));
        } catch (JSONException e) {
            Logging.warn("TapestryResponse", "Could not parse " + key + " in " + json);
            return new ArrayList<String>();
        }
    }

    public Map<String, List<String>> getStringListMap(String key) {
        try {
            HashMap<String, List<String>> map = new HashMap<String, List<String>>();
            JSONObject data = json.getJSONObject(key);
            for (String name : jsonArrayToStringList(data.names())) {
                map.put(name, jsonArrayToStringList(data.getJSONArray(name)));
            }
            return map;
        } catch (JSONException e) {
            Logging.warn("TapestryResponse", "Could not parse " + key + " in " + json);
            return new HashMap<String, List<String>>();
        }
    }

    private List<String> jsonArrayToStringList(JSONArray array) throws JSONException {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }

    @Override
    public String toString() {
        return json.toString();
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
