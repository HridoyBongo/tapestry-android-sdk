package com.tapad.tracking.deviceidentification;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManifestAggregator implements IdentifierSource {
    private IdentifierSource source;

    @Override
    public List<TypedIdentifier> get(Context context) {
        if (source == null)
            source = aggregateIdentifierSourcesFromManifest(context);
        return source.get(context);
    }

    private static IdentifierSource aggregateIdentifierSourcesFromManifest(Context context) {
        ArrayList<IdentifierSource> sources = new ArrayList<IdentifierSource>();
        try {
            Bundle metaData = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            // not configured, so default to android id
            if (metaData == null)
                return new AndroidId();
            String sourcesConfig = metaData.getString("tapad.ID_SOURCES");
            String[] idSourceClasses = (sourcesConfig == null ? "Android" : sourcesConfig).split(",");
            for (String className : idSourceClasses) {
                sources.add((IdentifierSource) Class.forName("com.tapad.tracking.deviceidentification." + className.trim()).newInstance());
            }
        } catch (Exception e) {
            com.tapad.util.Logging.warn(ManifestAggregator.class, "Unable to instantiate identifier sources from manifest: " + e);

        }
        return new IdentifierSourceAggregator(sources);
    }

}
