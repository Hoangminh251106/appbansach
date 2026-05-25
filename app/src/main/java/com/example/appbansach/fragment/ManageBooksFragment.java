package com.example.appbansach.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appbansach.R;
import com.example.appbansach.adapter.AdminBookAdapter;
import com.example.appbansach.databinding.FragmentManageBooksBinding;
import com.example.appbansach.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageBooksFragment extends Fragment {
    private FragmentManageBooksBinding binding;
    private AdminBookAdapter adapter;
    private List<Book> bookList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManageBooksBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadBooks();

        binding.fabAddBook.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_manageBooksFragment_to_addEditBookFragment));

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AdminBookAdapter(bookList, new AdminBookAdapter.OnBookActionListener() {
            @Override
            public void onEdit(Book book) {
                Bundle bundle = new Bundle();
                bundle.putString("bookId", book.getId());
                Navigation.findNavController(requireView()).navigate(R.id.action_manageBooksFragment_to_addEditBookFragment, bundle);
            }

            @Override
            public void onDelete(Book book) {
                showDeleteConfirmation(book);
            }
        });
        binding.rvAdminBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminBooks.setAdapter(adapter);
    }

    private void loadBooks() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("books").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                bookList.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Book book = doc.toObject(Book.class);
                    book.setId(doc.getId());
                    bookList.add(book);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showDeleteConfirmation(Book book) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sách")
                .setMessage("Bạn có chắc chắn muốn xóa cuốn sách '" + book.getTitle() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("books").document(book.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Đã xóa sách", Toast.LENGTH_SHORT).show();
                                loadBooks();
                            });
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
