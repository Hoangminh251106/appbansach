package com.example.appbansach.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.adapter.ShippingAdapter;
import com.example.appbansach.databinding.ActivityManageShippingBinding;
import com.example.appbansach.model.Order;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageShippingActivity extends AppCompatActivity {
    private ActivityManageShippingBinding binding;
    private FirebaseFirestore db;
    private List<Order> orderList = new ArrayList<>();
    private ShippingAdapter adapter;
    private String currentQuery = "";
    private String currentFilter = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageShippingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        setupUI();
        loadOrders();
    }

    private void setupUI() {
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }
        
        binding.rvShipping.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ShippingAdapter(this, orderList);
        binding.rvShipping.setAdapter(adapter);

        String[] filters = {"Tất cả", "Chờ xác nhận", "Đang giao", "Đã giao", "Đã hủy"};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filters);
        binding.spinnerStatus.setAdapter(spinAdapter);

        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                if (adapter != null) adapter.filter(currentQuery, currentFilter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                if (adapter != null) adapter.filter(currentQuery, currentFilter);
                return true;
            }
        });
    }

    private void loadOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (binding == null) return;
                        binding.progressBar.setVisibility(View.GONE);
                        
                        if (error != null) {
                            Toast.makeText(ManageShippingActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            orderList.clear();
                            int shipping = 0, success = 0, cancel = 0;

                            for (QueryDocumentSnapshot doc : value) {
                                Order order = doc.toObject(Order.class);
                                if (order != null) {
                                    order.setOrderId(doc.getId());
                                    orderList.add(order);

                                    String status = order.getStatus();
                                    if ("shipping".equals(status)) shipping++;
                                    else if ("delivered".equals(status)) success++;
                                    else if ("cancelled".equals(status)) cancel++;
                                }
                            }

                            binding.tvTotalShipping.setText(String.valueOf(shipping));
                            binding.tvTotalSuccess.setText(String.valueOf(success));
                            binding.tvTotalCancel.setText(String.valueOf(cancel));

                            adapter.updateList(orderList);
                            adapter.filter(currentQuery, currentFilter);
                        }
                    }
                });
    }
}
