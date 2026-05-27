package com.example.appbansach.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String role; // "customer" or "admin"
    private String status; // "active" or "locked"
    private List<String> wishlist = new ArrayList<>();
    private Timestamp createdAt;

    public User() {}

    public User(String uid, String fullName, String email, String phone, String address, String role, Timestamp createdAt) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.status = "active";
        this.createdAt = createdAt;
        this.wishlist = new ArrayList<>();
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getWishlist() { return wishlist; }
    public void setWishlist(List<String> wishlist) { this.wishlist = wishlist; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
