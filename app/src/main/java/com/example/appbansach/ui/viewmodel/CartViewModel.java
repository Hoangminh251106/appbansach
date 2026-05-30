package com.example.appbansach.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.appbansach.data.local.CartItemEntity;
import com.example.appbansach.helper.CartManager;
import java.util.List;

public class CartViewModel extends AndroidViewModel {
    private final CartManager cartManager;
    private final LiveData<List<CartItemEntity>> cartItems;

    public CartViewModel(@NonNull Application application) {
        super(application);
        cartManager = CartManager.getInstance(application);
        cartItems = cartManager.getCartItems();
    }

    public LiveData<List<CartItemEntity>> getCartItems() {
        return cartItems;
    }

    public void updateQuantity(String bookId, int quantity) {
        cartManager.updateQuantity(bookId, quantity);
    }

    public void removeFromCart(String bookId) {
        cartManager.removeFromCart(bookId);
    }

    public void updateSelection(String bookId, boolean isSelected) {
        cartManager.updateSelection(bookId, isSelected);
    }

    public void updateAllSelection(boolean isSelected) {
        cartManager.updateAllSelection(isSelected);
    }

    public void deleteSelectedItems() {
        cartManager.deleteSelectedItems();
    }
}
