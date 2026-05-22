package com.example.bookstore.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart_items")
public class CartItemEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String bookId;
    private String title;
    private String imageUrl;
    private long price;
    private int quantity;

    public CartItemEntity(String bookId, String title, String imageUrl, long price, int quantity) {
        this.bookId = bookId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}