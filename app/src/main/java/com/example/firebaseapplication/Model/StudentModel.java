package com.example.firebaseapplication.Model;

public class StudentModel {

    public String id;
    public String name;
    public double average;
    public String photo;
    public String jobTime;
    public String holiday = "0";

    public StudentModel() {
    }

    public StudentModel(String id, String name, double average, String photo,String jobTime, String holiday) {
        this.id = id;
        this.name = name;
        this.average = average;
        this.photo = photo;
        this.jobTime = jobTime;
        this.holiday = holiday;
    }
}
