package com.tapad.tapestry;

import com.tapad.util.Logging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TapestryResponse {
    private JSONObject json;

    public TapestryResponse(TapestryError error) {
        this("{errors:['" + error + "']}");
    }

    public TapestryResponse(String response) {
        try {
            json = new JSONObject(response);
        } catch (JSONException e) {
            Logging.warn(getClass(), "Could not parse " + response);
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

    public List<TapestryError> getErrors() {
        ArrayList<TapestryError> errors = new ArrayList<TapestryError>();
        for (String error : getList("errors"))
            errors.add(TapestryError.fromJSON(error));
        return errors;
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
            Logging.warn(getClass(), "Could not parse " + key + " in " + json);
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
            Logging.warn(getClass(), "Could not parse " + key + " in " + json);
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
        try {
            return json.toString(2);
        } catch (JSONException e) {
            return json.toString();
        }
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
