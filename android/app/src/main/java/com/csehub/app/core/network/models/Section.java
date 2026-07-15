package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

public class Section {

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("academicYear")
    private String academicYear;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAcademicYear() { return academicYear; }
    
    @Override
    public String toString() {
        return name;
    }
}
