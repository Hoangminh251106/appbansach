package com.example.bookstore.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentSearchBinding;
import com.example.bookstore.ui.home.BookAdapter;
import com.example.appbansach.utils.Resource;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private BookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        setupRecyclerView();
        setupSearchAndFilters();
        observeViewModel();
        
        if (getArguments() != null && getArguments().containsKey("categoryId")) {
            String categoryId = getArguments().getString("categoryId");
            viewModel.filterByCategory(categoryId);
        }
    }

    private void setupRecyclerView() {
        adapter = new BookAdapter(new ArrayList<>(), false, book -> {
            Bundle bundle = new Bundle();
            bundle.putString("bookId", book.getId());
            Navigation.findNavController(requireView()).navigate(R.id.bookDetailFragment, bundle);
        });
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvSearchResults.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            viewModel.filterByPrice(values.get(0), values.get(1));
        });
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                binding.chipGroupCategories.removeAllViews();
                
                Chip allChip = new Chip(getContext());
                allChip.setText("Tất cả");
                allChip.setCheckable(true);
                allChip.setChecked(true);
                allChip.setOnClickListener(v -> viewModel.filterByCategory(null));
                binding.chipGroupCategories.addView(allChip);

                for (com.example.bookstore.data.model.Category category : resource.data) {
                    Chip chip = new Chip(getContext());
                    chip.setText(category.getName());
                    chip.setCheckable(true);
                    chip.setOnClickListener(v -> viewModel.filterByCategory(category.getId()));
                    binding.chipGroupCategories.addView(chip);
                }
            }
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.layoutNoResults.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && !resource.data.isEmpty()) {
                        adapter = new BookAdapter(resource.data, false, book -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("bookId", book.getId());
                            Navigation.findNavController(requireView()).navigate(R.id.bookDetailFragment, bundle);
                        });
                        binding.rvSearchResults.setAdapter(adapter);
                        binding.layoutNoResults.setVisibility(View.GONE);
                    } else {
                        binding.rvSearchResults.setAdapter(new BookAdapter(new ArrayList<>(), false, null));
                        binding.layoutNoResults.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}