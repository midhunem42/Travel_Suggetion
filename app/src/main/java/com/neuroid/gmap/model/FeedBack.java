package com.neuroid.gmap.model;

import com.google.gson.annotations.SerializedName;

public class FeedBack {

    @SerializedName("lat")
    private String latitude;

    @SerializedName("lng")
    private String longitude;

    @SerializedName("feedback")
    private String feedback;

    public FeedBack(String latitude, String longitude, String feedback) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.feedback = feedback;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
