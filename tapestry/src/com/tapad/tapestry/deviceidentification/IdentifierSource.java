package com.tapad.tapestry.deviceidentification;

import android.content.Context;

import java.util.List;

/**
 * Finds identifiers from the device
 */
public interface IdentifierSource {
    /**
     * @return An collection of identifiers this source is able to produce
     */
    List<TypedIdentifier> get(Context context);
}
