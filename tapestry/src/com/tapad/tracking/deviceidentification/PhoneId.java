package com.tapad.tracking.deviceidentification;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.tapad.tapestry.Logging;

import java.util.ArrayList;
import java.util.List;

/**
 * This class knows how to fetch and encode values from telephonyManager.getDeviceId
 * <p/>
 * Gets the Phone ID hashed with MD5 and formatted as a 32 byte hexadecimal number.
 * Gets the Phone ID hashed with SHA1 and formatted as a 40 byte hexadecimal number.
 * <p/>
 * Usage of this class requires adding the following to the AndroidManifest.xml
 * <p/>
 * <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
 */
public class PhoneId implements IdentifierSource {
    @Override
    public List<TypedIdentifier> get(Context context) {
        List<TypedIdentifier> ids = new ArrayList<TypedIdentifier>();
        try {
        	TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String phoneId = telephonyManager.getDeviceId();
            ids.add(new TypedIdentifier(TypedIdentifier.TYPE_PHONE_ID_MD5, DigestUtil.md5Hash(phoneId)));
            ids.add(new TypedIdentifier(TypedIdentifier.TYPE_PHONE_ID_SHA1, DigestUtil.sha1Hash(phoneId)));
        } catch (Exception e) {
            Logging.error(getClass(), "Error retrieving PHONE_ID.", e);
        }
        return (ids);
    }
}
