package com.example.appbansach.model;

import com.google.firebase.Timestamp;

public class NotificationModel {
    private String id;
    private String title;
    private String content;
    private Timestamp sentAt;

    public NotificationModel() {}

    public NotificationModel(String title, String content, Timestamp sentAt) {
        this.title = title;
        this.content = content;
        this.sentAt = sentAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
}
