package com.example.appbansach.helper;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.example.appbansach.data.local.CartDao;
import com.example.appbansach.data.local.CartDatabase;
import com.example.appbansach.data.local.CartItemEntity;
import com.example.appbansach.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private CartDao cartDao;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private CartManager(Context context) {
        CartDatabase database = CartDatabase.getDatabase(context);
        cartDao = database.cartDao();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public static CartManager getInstance() {
        return instance;
    }

    public void addToCart(Book book) {
        CartDatabase.databaseWriteExecutor.execute(() -> {
            CartItemEntity existingItem = cartDao.getItemByBookId(book.getId());
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                cartDao.insertOrUpdate(existingItem);
            } else {
                CartItemEntity newItem = new CartItemEntity(
                        book.getId(),
                        book.getTitle(),
                        book.getImageUrl(),
                        book.getPrice(),
                        1
                );
                cartDao.insertOrUpdate(newItem);
            }
            syncCartToCloud();
        });
    }

    public LiveData<List<CartItemEntity>> getCartItems() {
        return cartDao.getAllCartItems();
    }

    // Tải dữ liệu từ Firestore về Room (Dùng khi đăng nhập)
    public void fetchCartFromCloud() {
        if (mAuth.getCurrentUser() == null) return;
        
        String uid = mAuth.getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> cloudItems = (List<Map<String, Object>>) documentSnapshot.get("currentCart");
                if (cloudItems != null) {
                    CartDatabase.databaseWriteExecutor.execute(() -> {
                        cartDao.clearCart();
                        for (Map<String, Object> item : cloudItems) {
                            String bookId = (String) item.get("bookId");
                            int quantity = ((Long) item.get("quantity")).intValue();
                            
                            // Lấy thông tin sách để cập nhật Room
                            db.collection("books").document(bookId).get().addOnSuccessListener(bookDoc -> {
                                if (bookDoc.exists()) {
                                    Book book = bookDoc.toObject(Book.class);
                                    CartDatabase.databaseWriteExecutor.execute(() -> {
                                        cartDao.insertOrUpdate(new CartItemEntity(
                                            bookId, book.getTitle(), book.getImageUrl(), book.getPrice(), quantity
                                        ));
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    public void updateQuantity(String bookId, int quantity) {
        CartDatabase.databaseWriteExecutor.execute(() -> {
            if (quantity <= 0) {
                cartDao.deleteItem(bookId);
            } else {
                cartDao.updateQuantity(bookId, quantity);
            }
            syncCartToCloud();
        });
    }

    public void removeFromCart(String bookId) {
        CartDatabase.databaseWriteExecutor.execute(() -> {
            cartDao.deleteItem(bookId);
            syncCartToCloud();
        });
    }

    public void clearCart() {
        CartDatabase.databaseWriteExecutor.execute(() -> {
            cartDao.clearCart();
            syncCartToCloud();
        });
    }

    private void syncCartToCloud() {
        if (mAuth.getCurrentUser() == null) return;
        
        String uid = mAuth.getUid();
        List<CartItemEntity> items = cartDao.getAllCartItemsList();
        
        List<Map<String, Object>> cloudItems = new ArrayList<>();
        for (CartItemEntity item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("bookId", item.getBookId());
            map.put("quantity", item.getQuantity());
            cloudItems.add(map);
        }
        
        db.collection("users").document(uid).update("currentCart", cloudItems);
    }
}
