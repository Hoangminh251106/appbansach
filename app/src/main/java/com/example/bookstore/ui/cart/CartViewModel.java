package com.example.bookstore.ui.cart;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.bookstore.data.local.CartDao;
import com.example.bookstore.data.local.CartDatabase;
import com.example.bookstore.data.local.CartItemEntity;

import java.util.List;

public class CartViewModel extends AndroidViewModel {
    private final CartDao cartDao;
    private final LiveData<List<CartItemEntity>> cartItems;

    public CartViewModel(@NonNull Application application) {
        super(application);
        cartDao = CartDatabase.getDatabase(application).cartDao();
        cartItems = cartDao.getAllCartItems();
    }

    public LiveData<List<CartItemEntity>> getCartItems() {
        return cartItems;
    }

    public LiveData<Long> getTotalPrice() {
        return Transformations.map(cartItems, items -> {
            long total = 0;
            for (CartItemEntity item : items) {
                total += item.getPrice() * item.getQuantity();
            }
            return total;
        });
    }

    public void updateQuantity(String bookId, int quantity) {
        CartDatabase.databaseWriteExecutor.execute(() -> cartDao.updateQuantity(bookId, quantity));
    }

    public void removeItem(String bookId) {
        CartDatabase.databaseWriteExecutor.execute(() -> cartDao.deleteItem(bookId));
    }

    public void clearCart() {
        CartDatabase.databaseWriteExecutor.execute(cartDao::clearCart);
    }
}