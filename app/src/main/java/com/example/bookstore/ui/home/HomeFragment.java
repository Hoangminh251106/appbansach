package com.example.bookstore.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentHomeBinding;
import com.example.appbansach.utils.Resource;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private BannerAdapter bannerAdapter;
    private CategoryAdapter categoryAdapter;
    private BookAdapter featuredAdapter;
    private BookAdapter newestAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerViews();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), category -> {
            Bundle bundle = new Bundle();
            bundle.putString("categoryId", category.getId());
            Navigation.findNavController(requireView()).navigate(R.id.searchFragment, bundle);
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        featuredAdapter = new BookAdapter(new ArrayList<>(), true, book -> navigateToDetail(book.getId()));
        binding.rvFeaturedBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedBooks.setAdapter(featuredAdapter);

        newestAdapter = new BookAdapter(new ArrayList<>(), false, book -> navigateToDetail(book.getId()));
        binding.rvNewestBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvNewestBooks.setAdapter(newestAdapter);
    }

    private void observeViewModel() {
        viewModel.getBanners().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                bannerAdapter = new BannerAdapter(resource.data);
                binding.viewPagerBanners.setAdapter(bannerAdapter);
            }
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                categoryAdapter = new CategoryAdapter(resource.data, category -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("categoryId", category.getId());
                    Navigation.findNavController(requireView()).navigate(R.id.searchFragment, bundle);
                });
                binding.rvCategories.setAdapter(categoryAdapter);
            }
        });

        viewModel.getFeaturedBooks().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                featuredAdapter = new BookAdapter(resource.data, true, book -> navigateToDetail(book.getId()));
                binding.rvFeaturedBooks.setAdapter(featuredAdapter);
            }
        });

        viewModel.getNewestBooks().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                newestAdapter = new BookAdapter(resource.data, false, book -> navigateToDetail(book.getId()));
                binding.rvNewestBooks.setAdapter(newestAdapter);
            }
        });
    }

    private void navigateToDetail(String bookId) {
        Bundle bundle = new Bundle();
        bundle.putString("bookId", bookId);
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bookDetailFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
