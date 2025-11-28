package com.youngariy.mopick.network.watchproviders;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WatchProviderRegion {

    @SerializedName("link")
    private String link;

    @SerializedName("flatrate")
    private List<WatchProvider> flatrate;

    @SerializedName("rent")
    private List<WatchProvider> rent;

    @SerializedName("buy")
    private List<WatchProvider> buy;

    public String getLink() {
        return link;
    }

    public List<WatchProvider> getFlatrate() {
        return flatrate;
    }

    public List<WatchProvider> getRent() {
        return rent;
    }

    public List<WatchProvider> getBuy() {
        return buy;
    }
}

