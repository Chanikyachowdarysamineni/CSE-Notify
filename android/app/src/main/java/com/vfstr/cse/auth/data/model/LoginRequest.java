package com.vfstr.cse.auth.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("loginId")
    private String loginId;

    @SerializedName("password")
    private String password;

    @SerializedName("fcmToken")
    private String fcmToken;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("deviceModel")
    private String deviceModel;

    @SerializedName("appVersion")
    private String appVersion;

    public LoginRequest(String loginId, String password, String fcmToken, String deviceId, String deviceModel, String appVersion) {
        this.loginId = loginId;
        this.password = password;
        this.fcmToken = fcmToken;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.appVersion = appVersion;
    }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public String getPassword() { return password; }
    public String getFcmToken() { return fcmToken; }
    
    public String getDeviceId() { return deviceId; }
    public String getDeviceModel() { return deviceModel; }
    public String getAppVersion() { return appVersion; }
}

