package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.adapter.BookAdapter;
import com.example.appbansach.databinding.FragmentBookListBinding;
import com.example.appbansach.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookListFragment extends Fragment {
    private FragmentBookListBinding binding;
    private BookAdapter adapter;
    private List<Book> bookList;
    private FirebaseFirestore db;
    private String categoryId;
    private String categoryName;
    private String type; // "featured", "newest", or "category"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookListBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            categoryName = getArguments().getString("categoryName");
            type = getArguments().getString("type");
        }

        setupToolbar();
        setupRecyclerView();
        loadBooks();

        return binding.getRoot();
    }

    private void setupToolbar() {
        String title = "Sách";
        if (categoryName != null) title = categoryName;
        else if ("featured".equals(type)) title = "Sách nổi bật";
        else if ("newest".equals(type)) title = "Sách mới nhất";

        binding.toolbar.setTitle(title);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        bookList = new ArrayList<>();
        adapter = new BookAdapter(bookList, book -> {
            Bundle bundle = new Bundle();
            bundle.putString("bookId", book.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_bookListFragment_to_bookDetailFragment, bundle);
        });
        binding.rvBookList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvBookList.setAdapter(adapter);
    }

    private void loadBooks() {
        Query query = db.collection("Books");

        if (categoryId != null) {
            query = query.whereEqualTo("categoryId", categoryId);
        } else if ("featured".equals(type)) {
            query = query.whereEqualTo("featured", true);
        } else if ("newest".equals(type)) {
            query = query.orderBy("createdAt", Query.Direction.DESCENDING);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                bookList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Book book = document.toObject(Book.class);
                    book.setId(document.getId());
                    bookList.add(book);
                }
                adapter.notifyDataSetChanged();
                binding.tvEmpty.setVisibility(bookList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
