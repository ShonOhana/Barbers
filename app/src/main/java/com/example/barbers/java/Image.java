package com.example.barbers.java;

import java.util.ArrayList;

public class Image {

    private String Uri;
    private int likes;
    private ArrayList<Like> likers;


    public Image() {}

    public Image(String uri, int likes, ArrayList<Like> likers) {
        Uri = uri;
        this.likes = likes;
        this.likers = likers;
    }



    public String getUri() {
        return Uri;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public ArrayList<Like> getLikers() {
        return likers;
    }

    public void setLikers(ArrayList<Like> likers) {
        this.likers = likers;
    }
}
