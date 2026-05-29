package com.example.appbansach.model;

public class Banner {
    private String id;
    private String imageUrl;
    private String targetBookId;
    private int order;

    public Banner() {}

    public Banner(String id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getTargetBookId() { return targetBookId; }
    public void setTargetBookId(String targetBookId) { this.targetBookId = targetBookId; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}