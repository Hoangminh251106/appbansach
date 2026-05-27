package com.example.appbansach.model;

import java.io.Serializable;
import java.util.Map;

public class ShippingModel implements Serializable {
    private String orderId;
    private String customerName;
    private String phone;
    private String address;
    private long shippingFee;
    private String shippingStatus;
    private long totalPrice;
    private String note;
    private long createdAt;
    private Map<String, Object> items;

    public ShippingModel() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public long getShippingFee() { return shippingFee; }
    public void setShippingFee(long shippingFee) { this.shippingFee = shippingFee; }
    public String getShippingStatus() { return shippingStatus; }
    public void setShippingStatus(String shippingStatus) { this.shippingStatus = shippingStatus; }
    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public Map<String, Object> getItems() { return items; }
    public void setItems(Map<String, Object> items) { this.items = items; }
}
