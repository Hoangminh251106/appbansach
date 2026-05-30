package com.example.appbansach.model;

public class ShippingCode {
    private String code;
    private long discountAmount; // Thường là 30000 để freeship
    private long minOrderAmount;

    public ShippingCode() {}

    public ShippingCode(String code, long discountAmount, long minOrderAmount) {
        this.code = code;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public long getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(long discountAmount) { this.discountAmount = discountAmount; }
    public long getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(long minOrderAmount) { this.minOrderAmount = minOrderAmount; }
}
