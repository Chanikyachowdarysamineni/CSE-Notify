package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

public class Timetable {

    @SerializedName("_id")
    private String id;

    @SerializedName("academicYear")
    private AcademicYear academicYear;

    @SerializedName("section")
    private Section section;

    @SerializedName("day")
    private String day;

    @SerializedName("period")
    private int period;

    @SerializedName("subject")
    private String subject;

    @SerializedName("subjectCode")
    private String subjectCode;

    @SerializedName("facultyName")
    private String facultyName;

    @SerializedName("room")
    private String room;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("type")
    private String type;

    public String getId() { return id; }
    public AcademicYear getAcademicYear() { return academicYear; }
    public Section getSection() { return section; }
    public String getDay() { return day; }
    public int getPeriod() { return period; }
    public String getSubject() { return subject; }
    public String getSubjectCode() { return subjectCode; }
    public String getFacultyName() { return facultyName; }
    public String getRoom() { return room; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getType() { return type; }
}
