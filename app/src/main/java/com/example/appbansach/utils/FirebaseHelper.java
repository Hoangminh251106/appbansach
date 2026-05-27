package com.example.appbansach.utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Map;

public class FirebaseHelper {
    private final DatabaseReference mDatabase;

    public FirebaseHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.NODE_ORDERS);
    }

    public DatabaseReference getOrdersRef() {
        return mDatabase;
    }

    public Task<Void> updateOrderStatus(String orderId, String status) {
        return mDatabase.child(orderId).child("shippingStatus").setValue(status);
    }

    public Task<Void> updateShippingFee(String orderId, long fee) {
        return mDatabase.child(orderId).child("shippingFee").setValue(fee);
    }

    public Task<Void> deleteOrder(String orderId) {
        return mDatabase.child(orderId).removeValue();
    }
}
