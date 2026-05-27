package com.example.appbansach.model;

import com.google.firebase.Timestamp;

public class Review {
    private String bookId;
    private String userId;
    private String userName;
    private String userAvatar;
    private float rating;
    private String comment;
    private Timestamp createdAt;

    public Review() {}

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    // Giữ lại để tương thích với code cũ nếu có
    public Timestamp getTimestamp() { return createdAt; }
}
