package com.example.barbers.java;

public class Like {

    private String fuserID;
    private String name;

    public Like(String fuserID, String name) {
        this.fuserID = fuserID;
        this.name = name;
    }

    public Like() {
    }

    public String getFuserID() {
        return fuserID;
    }

    public void setFuserID(String fuserID) {
        this.fuserID = fuserID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
