package com.tapad.tapestry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Contains information about this device and devices connected to it in the device graph, as a result of sending a
 * request to {@link TapestryClient}.  Accessor methods return a list of values from all devices.
 * <p/>
 * Examples of getting information from a response:
 * <blockquote><pre>
 * if (response.getData("color").contains("blue"))
 *  // user has a preference for blue
 * if (response.getData("color").isEmpty())
 *  // user has no color preferences yet
 * if (response.getAudiences().contains("buying-car"))
 *   // user is in a buying car audience
 * for (String cookieId : response.getIds("my-cookie"))
 *   // get every cookie id from connected devices
 * if (response.getPlatforms().contains("XBox"))
 *   // user has an XBox
 * if (response.getErrors().size() > 0)
 *   // errors occurred
 * for (TapestryResponse device : response.getDevices())
 *   // get each device as a separate response
 * </pre></blockquote>
 */
public class TapestryResponse {
    private JSONObject json;

    protected TapestryResponse(TapestryError error) {
        this("{errors:['" + error + "']}");
    }

    /**
     * Creates a response from a JSON string returned by Tapestry
     *
     * @param jsonString JSON string
     */
    public TapestryResponse(String jsonString) {
        try {
            json = new JSONObject(jsonString);
        } catch (Exception e) {
            Logging.warn(getClass(), "Could not parse " + jsonString);
            json = new JSONObject();
        }
    }

    /**
     * Returns information about each device separately as a list of responses.  For instance, whereas {@code
     * getPlatforms()} normally returns a list of platforms from all devices, calling {@code getPlatforms()} on a
     * response returned by this method will return only the platform for a single device.
     * <p/>
     * Note that {@link com.tapad.tapestry.TapestryRequest#listDevices()} must be called on the request in order for the
     * devices to be contained in the response.  Calling {@code getDevices()} on devices returned by this method will
     * always return an empty list.
     *
     * @return a list of devices connected to this one or an empty list if none exist
     */
    public List<TapestryResponse> getDevices() {
        ArrayList<TapestryResponse> devices = new ArrayList<TapestryResponse>();
        for (String device : getList("devices"))
            devices.add(new TapestryResponse(device));
        return devices;
    }

    /**
     * Returns the platforms the devices.  Not all devices are guaranteed to return a platform.
     *
     * @return a list of platform names
     */
    public List<String> getPlatforms() {
        return getList("platforms");
    }

    /**
     * Returns any errors that occurred when handling this request.  The types of errors can be found in the {@link
     * TapestryError} class.
     *
     * @return a list of errors
     */
    public List<TapestryError> getErrors() {
        ArrayList<TapestryError> errors = new ArrayList<TapestryError>();
        for (String error : getList("errors"))
            errors.add(TapestryError.fromJSON(error));
        return errors;
    }

    /**
     * Returns the audiences that the devices are members of.
     *
     * @return a list of audiences
     */
    public List<String> getAudiences() {
        return getList("audiences");
    }

    /**
     * Returns the values associated with a given key in the devices.
     *
     * @param key The key to retrieve from devices
     * @return a list of values
     */
    public List<String> getData(String key) {
        return getStringListMap("data", key);
    }

    /**
     * Returns the hardware or cookie ids of the devices.
     *
     * @param type The id type to retrieve
     * @return a list of ids
     */
    public List<String> getIds(String type) {
        return getStringListMap("ids", type);
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
