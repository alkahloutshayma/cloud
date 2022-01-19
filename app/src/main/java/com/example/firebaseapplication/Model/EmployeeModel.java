package com.example.firebaseapplication.Model;

public class EmployeeModel {

    public String id;
    public String name;
    public double salary;
    public String photo;
    public String jobTime;
    public String holiday = "0";

    public EmployeeModel() {
    }

    public EmployeeModel(String id, String name, double salary , String photo, String jobTime, String holiday) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.photo = photo;
        this.jobTime = jobTime;
        this.holiday = holiday;
    }
}
