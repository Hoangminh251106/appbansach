package com.example.appbansach.model;

import com.google.firebase.Timestamp;

public class NotificationModel {
    private String id;
    private String title;
    private String content;
    private String userId; // Thêm userId để gửi đến đúng người dùng
    private Timestamp sentAt;
    private boolean isRead;

    public NotificationModel() {}

    public NotificationModel(String title, String content, String userId, Timestamp sentAt) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.sentAt = sentAt;
        this.isRead = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
