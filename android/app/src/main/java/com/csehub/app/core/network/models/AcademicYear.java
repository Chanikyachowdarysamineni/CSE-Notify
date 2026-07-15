package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

public class AcademicYear {

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("session")
    private String session;

    @SerializedName("order")
    private int order;

    @SerializedName("isActive")
    private boolean isActive;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSession() { return session; }
    public int getOrder() { return order; }
    public boolean isActive() { return isActive; }
    
    @Override
    public String toString() {
        if (session != null && !session.isEmpty()) {
            return name + " (" + session + ")";
        }
        return name;
    }
}
