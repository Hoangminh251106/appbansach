package com.example.appbansach.model;

public class Voucher {
    private String code;
    private long discountAmount;
    private long minOrderAmount;

    public Voucher() {}

    public Voucher(String code, long discountAmount, long minOrderAmount) {
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
