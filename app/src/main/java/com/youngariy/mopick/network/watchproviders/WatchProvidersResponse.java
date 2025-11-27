package com.youngariy.mopick.network.watchproviders;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class WatchProvidersResponse {

    @SerializedName("results")
    private Map<String, WatchProviderRegion> results;

    public Map<String, WatchProviderRegion> getResults() {
        return results;
    }

    public WatchProviderRegion getRegion(String regionCode) {
        if (results == null || regionCode == null) return null;
        return results.get(regionCode);
    }
}

