package com.csehub.app.auth.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private UserInfo user;

    public String getToken() { return token; }
    public UserInfo getUser() { return user; }

    public static class UserInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("email")
        private String email;

        @SerializedName("name")
        private String name;

        @SerializedName("role")
        private String role;

        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getRole() { return role; }
    }
}
