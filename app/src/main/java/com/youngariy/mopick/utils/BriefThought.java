package com.youngariy.mopick.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Brief Thought data model
 */
public class BriefThought {
    private int id;
    private int contentId;
    private String contentType; // "movie" or "tv_show"
    private String contentTitle;
    private String status; // "watching" or "completed"
    private int rating; // 1-5
    private List<String> moods; // emoji codes
    private String recommend; // "yes" or "no"
    private long createdAt;

    public BriefThought() {
        this.moods = new ArrayList<>();
    }

    public BriefThought(int id, int contentId, String contentType, String contentTitle,
                       String status, int rating, List<String> moods, String recommend, long createdAt) {
        this.id = id;
        this.contentId = contentId;
        this.contentType = contentType;
        this.contentTitle = contentTitle;
        this.status = status;
        this.rating = rating;
        this.moods = moods != null ? moods : new ArrayList<>();
        this.recommend = recommend;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<String> getMoods() {
        return moods;
    }

    public void setMoods(List<String> moods) {
        this.moods = moods != null ? moods : new ArrayList<>();
    }

    public String getRecommend() {
        return recommend;
    }

    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}

