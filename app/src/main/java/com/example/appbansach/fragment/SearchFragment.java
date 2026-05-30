package com.example.appbansach.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.databinding.FragmentSearchBinding;
import com.example.appbansach.model.Book;
import com.example.appbansach.ui.viewmodel.BookViewModel;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private BookAdapter adapter;
    private BookViewModel viewModel;
    private FirebaseFirestore db;
    
    private final List<Book> fullList = new ArrayList<>();
    private final List<Book> displayList = new ArrayList<>();
    
    private float minPrice = 0;
    private float maxPrice = 1000000;
    
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(BookViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        loadCategories();
        observeViewModel();

        if (getArguments() != null) {
            if (getArguments().containsKey("category_name")) {
                String category = getArguments().getString("category_name");
                binding.etSearch.setText(category);
                viewModel.searchByCategory(category); // Sử dụng hàm tìm kiếm theo category chính xác hơn
            } else if (getArguments().containsKey("query")) {
                String query = getArguments().getString("query");
                binding.etSearch.setText(query);
                viewModel.searchBooks(query);
            }
        } else {
            viewModel.searchBooks("Sách");
        }
    }

    private void observeViewModel() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null) {
                        fullList.clear();
                        fullList.addAll(resource.data);
                        updateUiList();
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void setupUI() {
        adapter = new BookAdapter(displayList, book -> {
            db.collection("books").document(book.getId()).set(book)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded() && binding != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("bookId", book.getId());
                            Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_bookDetailFragment, bundle);
                        }
                    });
        });
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvSearchResults.setAdapter(adapter);
        binding.ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 3) {
                    searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> viewModel.searchBooks(query);
                    searchHandler.postDelayed(searchRunnable, 800);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minPrice = values.get(0);
            maxPrice = values.get(1);
            updateUiList();
        });
    }

    private void updateUiList() {
        if (binding == null) return;
        displayList.clear();
        for (Book b : fullList) {
            if (b.getPrice() >= minPrice && b.getPrice() <= maxPrice) {
                displayList.add(b);
            }
        }
        adapter.notifyDataSetChanged();
        binding.layoutNoResults.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!isAdded() || binding == null) return;
            binding.chipGroupCategories.removeAllViews();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name == null) continue;
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip_category, binding.chipGroupCategories, false);
                chip.setText(name);
                chip.setOnClickListener(v -> {
                    binding.etSearch.setText(name);
                    viewModel.searchByCategory(name);
                });
                binding.chipGroupCategories.addView(chip);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        searchHandler.removeCallbacksAndMessages(null);
    }
}
