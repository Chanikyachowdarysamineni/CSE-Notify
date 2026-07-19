package com.csehub.app.academic.models;

import com.google.gson.annotations.SerializedName;

public class AcademicYear {
    @SerializedName("_id")
    private String id;
    
    private String name;
    private String session;
    private String status;
    private int order;

    public AcademicYear(String name, String session, String status, int order) {
        this.name = name;
        this.session = session;
        this.status = status;
        this.order = order;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSession() { return session; }
    public void setSession(String session) { this.session = session; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    @Override
    public String toString() {
        return name + " (" + session + ")"; // For Spinner display
    }
}
