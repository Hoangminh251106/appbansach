package com.example.bookstore.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookstore.data.model.Order;
import com.example.appbansach.utils.Resource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Resource<String>> placeOrder(Order order) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Using a transaction to ensure stock consistency
        db.runTransaction(transaction -> {
            // 1. Check and Update Stock
            for (Map<String, Object> item : order.getItems()) {
                String bookId = (String) item.get("bookId");
                int quantity = (int) item.get("quantity");
                
                DocumentReference bookRef = db.collection("books").document(bookId);
                long currentStock = transaction.get(bookRef).getLong("stock");
                
                if (currentStock < quantity) {
                    throw new RuntimeException("Sách '" + item.get("title") + "' đã hết hàng hoặc không đủ số lượng.");
                }
                
                transaction.update(bookRef, "stock", currentStock - quantity);
            }

            // 2. Save Order
            DocumentReference orderRef = db.collection("orders").document(order.getOrderId());
            transaction.set(orderRef, order);
            
            return order.getOrderId();
        }).addOnSuccessListener(orderId -> {
            result.setValue(Resource.success(orderId));
        }).addOnFailureListener(e -> {
            result.setValue(Resource.error(e.getMessage(), null));
        });
        
        return result;
    }

    public LiveData<Resource<List<Order>>> getOrderHistory(String userId) {
        MutableLiveData<Resource<List<Order>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            order.setOrderId(document.getId());
                            orders.add(order);
                        }
                        result.setValue(Resource.success(orders));
                    } else {
                        result.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
        return result;
    }
}