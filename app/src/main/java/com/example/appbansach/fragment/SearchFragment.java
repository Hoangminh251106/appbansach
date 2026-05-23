package com.example.appbansach.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.databinding.FragmentSearchBinding;
import com.example.appbansach.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private BookAdapter adapter;
    private List<Book> bookList;
    private List<Book> filteredList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadAllBooks();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBooks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Xử lý nút quay lại
        binding.ivBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        bookList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new BookAdapter(filteredList, book -> {
            Bundle bundle = new Bundle();
            bundle.putString("bookId", book.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_bookDetailFragment, bundle);
        });
        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSearchResults.setAdapter(adapter);
    }

    private void loadAllBooks() {
        if (binding.progressBar != null) binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("books").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (isAdded()) {
                if (binding.progressBar != null) binding.progressBar.setVisibility(View.GONE);
                bookList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Book book = doc.toObject(Book.class);
                    book.setId(doc.getId());
                    bookList.add(book);
                }
            }
        }).addOnFailureListener(e -> {
            if (isAdded() && binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void filterBooks(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            binding.layoutNoResults.setVisibility(View.GONE);
        } else {
            for (Book book : bookList) {
                if (book.getTitle().toLowerCase().contains(query.toLowerCase()) || 
                    book.getAuthor().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(book);
                }
            }
            binding.layoutNoResults.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
