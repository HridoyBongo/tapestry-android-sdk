package com.tapad.tapestry.deviceidentification;

/**
 * An identifier value packaged with a type
 */
public class TypedIdentifier {

    /**
     * List of known identifier types
     */
    public static final String TYPE_RANDOM_UUID = "pref";
    public static final String TYPE_ANDROID_ID_SHA1 = "sha1anid";
    public static final String TYPE_ANDROID_ID_MD5 = "md5anid";
    public static final String TYPE_PHONE_ID_SHA1 = "sha1phid";
    public static final String TYPE_PHONE_ID_MD5 = "md5phid";
    public static final String TYPE_WIFI_MAC_SHA1 = "sha1mac";
    public static final String TYPE_WIFI_MAC_MD5 = "md5mac";

    private String type = null;
    private String value = null;

    public TypedIdentifier(String type, String value) {
        this.type = type;
        this.value = value;
    }
    /**
     * @return the type of the identifier
     */
    public String getType() { return (this.type); }

    /**
     * @return the value of the identifier
     */
    public String getValue() { return (this.value); }

    /**
     * @return the single string representation
     */
    public String toString() { return (this.type + ":" + this.value); }
}
