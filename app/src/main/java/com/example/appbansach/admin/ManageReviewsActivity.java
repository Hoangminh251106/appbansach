package com.example.appbansach.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.ReviewAdapter;
import com.example.appbansach.databinding.ActivityManageReviewsBinding;
import com.example.appbansach.model.ReviewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageReviewsActivity extends AppCompatActivity {
    private ActivityManageReviewsBinding binding;
    private FirebaseFirestore db;
    private List<ReviewModel> reviewList = new ArrayList<>();
    private ReviewAdapter adapter;
    private String currentQuery = "";
    private float currentRatingFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        loadReviews();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ReviewAdapter(reviewList, this::confirmDeleteReview);
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(adapter);
    }

    private void setupFilters() {
        String[] ratings = {"Tất cả", "5 sao", "4 sao", "3 sao", "2 sao", "1 sao"};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ratings);
        binding.spinnerRating.setAdapter(spinAdapter);

        binding.spinnerRating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) currentRatingFilter = 0;
                else currentRatingFilter = 6 - position;
                adapter.filter(currentRatingFilter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                // Note: The adapter filter currently only handles rating. 
                // In a production app, we would combine search + filter.
                // For simplicity, I'll update the adapter.
                loadReviews(); 
                return true;
            }
        });
    }

    private void loadReviews() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (error != null) return;

                    if (value != null) {
                        reviewList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            ReviewModel review = doc.toObject(ReviewModel.class);
                            review.setReviewId(doc.getId());
                            
                            // Simple text search filter
                            if (currentQuery.isEmpty() || 
                                review.getBookTitle().toLowerCase().contains(currentQuery.toLowerCase()) ||
                                review.getContent().toLowerCase().contains(currentQuery.toLowerCase())) {
                                reviewList.add(review);
                            }
                        }
                        adapter.updateList(reviewList);
                        adapter.filter(currentRatingFilter);
                        
                        binding.tvEmpty.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void confirmDeleteReview(ReviewModel review) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("reviews").document(review.getReviewId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
