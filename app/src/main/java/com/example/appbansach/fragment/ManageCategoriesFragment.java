package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.adapter.AdminCategoryAdapter;
import com.example.appbansach.databinding.FragmentManageCategoriesBinding;
import com.example.appbansach.model.Category;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesFragment extends Fragment {
    private FragmentManageCategoriesBinding binding;
    private AdminCategoryAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageCategoriesBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadCategories();

        binding.fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoryAdapter(categoryList, category -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xóa danh mục")
                    .setMessage("Xóa danh mục '" + category.getName() + "'?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        db.collection("categories").document(category.getId()).delete()
                                .addOnSuccessListener(v -> Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        binding.rvAdminCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminCategories.setAdapter(adapter);
    }

    private void loadCategories() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("categories").addSnapshotListener((value, error) -> {
            if (!isAdded()) return;
            binding.progressBar.setVisibility(View.GONE);
            if (value != null) {
                categoryList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Category cat = doc.toObject(Category.class);
                    cat.setId(doc.getId());
                    categoryList.add(cat);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddCategoryDialog() {
        EditText etName = new EditText(requireContext());
        etName.setHint("Tên danh mục mới");
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm danh mục")
                .setView(etName)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        Category c = new Category("", name, "https://cdn-icons-png.flaticon.com/512/3389/3389081.png");
                        db.collection("categories").add(c);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
