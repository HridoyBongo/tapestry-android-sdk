package com.tapad.tapestry;

import com.tapad.util.Logging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the response to requests sent by the {@link TapestryClient}.
 * <p/>
 * Responses are returned as JSON from the Tapestry Web API.  This class parses that JSON and provides useful accessors
 * to the fields contained within.
 * <p/>
 * An example of getting information from a response:
 * <blockquote><pre>
 * if (response.getData("color").contains("blue"))
 *  // user has a preference for blue
 * if (response.getData("color").isEmpty())
 *  // user has no color preferences yet
 * if (response.getAudiences().contains("buying-car"))
 *   // user is in a buying car audience
 * for (String cookieId : response.getIds("my-cookie"))
 *   // for every cookie id in a connected device the user has
 * if (response.getPlatforms().contains("XBox"))
 *   // user has an XBox
 * if (response.getErrors().isEmpty())
 *   // no errors occurred
 * for (TapestryResponse device : response.getDevices())
 *   // handle each device separately
 * </pre></blockquote>
 */
public class TapestryResponse {
    private JSONObject json;

    public TapestryResponse(TapestryError error) {
        this("{errors:['" + error + "']}");
    }

    public TapestryResponse(String response) {
        try {
            json = new JSONObject(response);
        } catch (Exception e) {
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

    public List<String> getData(String key) {
        return getStringListMap("data", key);
    }

    public List<String> getIds(String key) {
        return getStringListMap("ids", key);
    }

    private List<String> getList(String key) {
        try {
            return jsonArrayToStringList(json.getJSONArray(key));
        } catch (Exception e) {
            Logging.warn(getClass(), "Could not parse " + key + " in " + json);
            return new ArrayList<String>();
        }
    }

    private List<String> getStringListMap(String name, String key) {
        try {
            HashMap<String, List<String>> map = new HashMap<String, List<String>>();
            JSONObject data = json.getJSONObject(name);
            return jsonArrayToStringList(data.getJSONArray(key));
        } catch (Exception e) {
            Logging.warn(getClass(), "Could not parse " + name + " in " + json);
            return new ArrayList<String>();
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
