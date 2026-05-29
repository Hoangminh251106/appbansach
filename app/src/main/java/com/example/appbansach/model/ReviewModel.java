package com.example.appbansach.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class ReviewModel {
    private String reviewId, userId, userName, bookId, bookTitle, content, adminReply;
    private float rating;
    private Timestamp createdAt, repliedAt;

    public ReviewModel() {}

    public ReviewModel(String userName, float rating, String content, Timestamp createdAt) {
        this.userName = userName;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    @PropertyName("content")
    public String getContent() { return content; }
    @PropertyName("content")
    public void setContent(String content) { this.content = content; }

    @PropertyName("comment")
    public String getComment() { return content; }
    @PropertyName("comment")
    public void setComment(String comment) { this.content = comment; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    @PropertyName("createdAt")
    public Timestamp getCreatedAt() { return createdAt; }
    @PropertyName("createdAt")
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @PropertyName("timestamp")
    public Timestamp getTimestamp() { return createdAt; }
    @PropertyName("timestamp")
    public void setTimestamp(Timestamp timestamp) { this.createdAt = timestamp; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public Timestamp getRepliedAt() { return repliedAt; }
    public void setRepliedAt(Timestamp repliedAt) { this.repliedAt = repliedAt; }
}
