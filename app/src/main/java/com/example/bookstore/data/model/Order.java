package com.example.bookstore.data.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class Order {
    private String orderId;
    private String userId;
    private List<Map<String, Object>> items; // {bookId, title, imageUrl, price, quantity}
    private long totalAmount;
    private long shippingFee;
    private Map<String, String> shippingAddress; // {name, phone, address}
    private String paymentMethod;
    private String status; // "pending", "shipping", "delivered", "cancelled"
    private Timestamp createdAt;

    public Order() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
    public long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }
    public long getShippingFee() { return shippingFee; }
    public void setShippingFee(long shippingFee) { this.shippingFee = shippingFee; }
    public Map<String, String> getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Map<String, String> shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}