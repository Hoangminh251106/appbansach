package com.example.appbansach.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appbansach.model.Order;
import com.example.appbansach.utils.Resource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Resource<String>> placeOrder(Order order) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("orders").document(order.getOrderId()).set(order)
                .addOnSuccessListener(aVoid -> result.setValue(Resource.success(order.getOrderId())))
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));
        
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