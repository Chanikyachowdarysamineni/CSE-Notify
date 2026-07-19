package com.csehub.app.academic.models;

import com.google.gson.annotations.SerializedName;

public class Section {
    @SerializedName("_id")
    private String id;
    
    private String name;
    private String academicYear; // ObjectId string
    private String status;
    private int capacity;
    private int order;

    public Section(String name, String academicYear, String status, int capacity, int order) {
        this.name = name;
        this.academicYear = academicYear;
        this.status = status;
        this.capacity = capacity;
        this.order = order;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    @Override
    public String toString() {
        return name; // For Spinner display
    }
}
