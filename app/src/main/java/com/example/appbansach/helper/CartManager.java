package com.example.appbansach.helper;

import com.example.appbansach.model.Book;
import com.example.appbansach.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Book book) {
        for (CartItem item : cartItems) {
            if (item.getBook().getId().equals(book.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartItems.add(new CartItem(book, 1));
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void removeProduct(int position) {
        cartItems.remove(position);
    }

    public void removeFromCart(String bookId) {
        cartItems.removeIf(item -> item.getBook().getId().equals(bookId));
    }

    public void clearCart() {
        cartItems.clear();
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }
}