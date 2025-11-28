package com.youngariy.mopick.network.watchproviders;

import com.google.gson.annotations.SerializedName;

public class WatchProvider {

    @SerializedName("provider_id")
    private Integer providerId;

    @SerializedName("provider_name")
    private String providerName;

    @SerializedName("logo_path")
    private String logoPath;

    public Integer getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getLogoPath() {
        return logoPath;
    }
}

