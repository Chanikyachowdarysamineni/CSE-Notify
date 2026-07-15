package com.csehub.app.auth.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("loginId")
    private String loginId;

    @SerializedName("password")
    private String password;

    @SerializedName("fcmToken")
    private String fcmToken;

    public LoginRequest(String loginId, String password, String fcmToken) {
        this.loginId = loginId;
        this.password = password;
        this.fcmToken = fcmToken;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() { return password; }
    public String getFcmToken() { return fcmToken; }
}
