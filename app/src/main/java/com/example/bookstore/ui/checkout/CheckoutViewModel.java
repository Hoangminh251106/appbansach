package com.example.bookstore.ui.checkout;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookstore.data.local.CartDao;
import com.example.bookstore.data.local.CartDatabase;
import com.example.bookstore.data.local.CartItemEntity;
import com.example.bookstore.data.model.Order;
import com.example.bookstore.data.repository.OrderRepository;
import com.example.bookstore.data.repository.UserRepository;
import com.example.appbansach.utils.Resource;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckoutViewModel extends AndroidViewModel {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartDao cartDao;
    private final LiveData<List<CartItemEntity>> cartItems;

    public CheckoutViewModel(@NonNull Application application) {
        super(application);
        this.orderRepository = new OrderRepository();
        this.userRepository = new UserRepository();
        this.cartDao = CartDatabase.getDatabase(application).cartDao();
        this.cartItems = cartDao.getAllCartItems();
    }

    public LiveData<List<CartItemEntity>> getCartItems() {
        return cartItems;
    }

    public LiveData<Resource<String>> placeOrder(String name, String phone, String address, String paymentMethod) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        List<CartItemEntity> items = cartItems.getValue();
        
        if (items == null || items.isEmpty()) {
            result.setValue(Resource.error("Giỏ hàng trống", null));
            return result;
        }

        long totalAmount = 0;
        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (CartItemEntity item : items) {
            totalAmount += item.getPrice() * item.getQuantity();
            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("bookId", item.getBookId());
            orderItem.put("title", item.getTitle());
            orderItem.put("imageUrl", item.getImageUrl());
            orderItem.put("price", item.getPrice());
            orderItem.put("quantity", item.getQuantity());
            orderItems.add(orderItem);
        }

        String orderId = UUID.randomUUID().toString();
        String userId = userRepository.getCurrentUid();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setShippingFee(30000); // Mặc định 30k
        Map<String, String> shipping = new HashMap<>();
        shipping.put("name", name);
        shipping.put("phone", phone);
        shipping.put("address", address);
        order.setShippingAddress(shipping);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("pending");
        order.setCreatedAt(Timestamp.now());

        return orderRepository.placeOrder(order);
    }

    public void clearCart() {
        CartDatabase.databaseWriteExecutor.execute(cartDao::clearCart);
    }
}