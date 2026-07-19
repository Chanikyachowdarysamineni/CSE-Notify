package com.csehub.app.auth.data.model;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenRequest {
    @SerializedName("fcmToken")
    private String fcmToken;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("deviceModel")
    private String deviceModel;

    @SerializedName("appVersion")
    private String appVersion;

    public RefreshTokenRequest(String fcmToken, String deviceId, String deviceModel, String appVersion) {
        this.fcmToken = fcmToken;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.appVersion = appVersion;
    }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    
    public String getDeviceId() { return deviceId; }
    public String getDeviceModel() { return deviceModel; }
    public String getAppVersion() { return appVersion; }
}
