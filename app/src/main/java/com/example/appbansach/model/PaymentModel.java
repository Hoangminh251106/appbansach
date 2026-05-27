package com.example.appbansach.model;

import com.google.firebase.Timestamp;

public class PaymentModel {
    private String transactionId;
    private String userId;
    private String userName;
    private long amount;
    private String method; // "ZaloPay", "Momo", "COD"
    private String status; // "Success", "Pending", "Failed"
    private Timestamp createdAt;

    public PaymentModel() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
