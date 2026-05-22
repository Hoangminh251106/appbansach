package com.example.appbansach.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.activity.BookDetailActivity;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.adapter.CategoryAdapter;
import com.example.appbansach.databinding.FragmentHomeBinding;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Category;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private BookAdapter bookAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Book> bookList;
    private List<Category> categoryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerViews();
        loadCategories();
        loadBooks();

        return binding.getRoot();
    }

    private void setupRecyclerViews() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            filterBooksByCategory(category.getId());
        });
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList, book -> {
            Intent intent = new Intent(getActivity(), BookDetailActivity.class);
            intent.putExtra("book", book);
            startActivity(intent);
        });
        // Sửa rvBooks thành rvNewestBooks để khớp với fragment_home.xml
        binding.rvNewestBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvNewestBooks.setAdapter(bookAdapter);
    }

    private void loadCategories() {
        db.collection("Categories").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoryList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Category category = document.toObject(Category.class);
                    category.setId(document.getId());
                    categoryList.add(category);
                }
                categoryAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadBooks() {
        db.collection("Books").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                bookList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Book book = document.toObject(Book.class);
                    book.setId(document.getId());
                    bookList.add(book);
                }
                bookAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Lỗi tải sách", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterBooksByCategory(String categoryId) {
        db.collection("Books").whereEqualTo("categoryId", categoryId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                bookList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Book book = document.toObject(Book.class);
                    book.setId(document.getId());
                    bookList.add(book);
                }
                bookAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
