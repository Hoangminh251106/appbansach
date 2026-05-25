package com.example.appbansach.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CartDao {
    @Query("SELECT * FROM cart_items")
    LiveData<List<CartItemEntity>> getAllCartItems();

    @Query("SELECT * FROM cart_items")
    List<CartItemEntity> getAllCartItemsList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(CartItemEntity item);

    @Query("UPDATE cart_items SET quantity = :quantity WHERE bookId = :bookId")
    void updateQuantity(String bookId, int quantity);

    @Query("DELETE FROM cart_items WHERE bookId = :bookId")
    void deleteItem(String bookId);

    @Query("DELETE FROM cart_items")
    void clearCart();

    @Query("SELECT * FROM cart_items WHERE bookId = :bookId LIMIT 1")
    CartItemEntity getItemByBookId(String bookId);
}
