package com.example.appbansach.model;

import com.google.firebase.Timestamp;

public class Review {
    private String userName;
    private float rating;
    private String comment;
    private Timestamp timestamp;

    public Review() {}

    public Review(String userName, float rating, String comment, Timestamp timestamp) {
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
