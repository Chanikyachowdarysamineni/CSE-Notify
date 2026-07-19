package com.csehub.app.auth.data.model;

import com.google.gson.annotations.SerializedName;

public class LogoutRequest {

    @SerializedName("fcmToken")
    private String fcmToken;

    public LogoutRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
