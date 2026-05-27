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
import com.example.appbansach.databinding.FragmentWishlistBinding;
import com.example.appbansach.model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {
    private FragmentWishlistBinding binding;
    private BookAdapter adapter;
    private List<Book> wishlistBooks = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration wishlistListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        startListeningWishlist();

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new BookAdapter(wishlistBooks, book -> {
            Bundle bundle = new Bundle();
            bundle.putString("bookId", book.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_wishlistFragment_to_bookDetailFragment, bundle);
        });
        binding.rvWishlist.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvWishlist.setAdapter(adapter);
    }

    private void startListeningWishlist() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        // Sử dụng addSnapshotListener để cập nhật ngay lập tức khi nhấn yêu thích ở nơi khác
        wishlistListener = db.collection("users").document(userId).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null || documentSnapshot == null || !documentSnapshot.exists()) return;

            List<String> wishlistIds = (List<String>) documentSnapshot.get("wishlist");
            if (wishlistIds == null || wishlistIds.isEmpty()) {
                if (isAdded()) {
                    binding.layoutEmptyWishlist.setVisibility(View.VISIBLE);
                    wishlistBooks.clear();
                    adapter.notifyDataSetChanged();
                }
            } else {
                if (isAdded()) {
                    binding.layoutEmptyWishlist.setVisibility(View.GONE);
                    fetchBooks(wishlistIds);
                }
            }
        });
    }

    private void fetchBooks(List<String> ids) {
        // Lấy toàn bộ sách và lọc để đảm bảo dữ liệu mới nhất
        db.collection("books").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!isAdded()) return;
            wishlistBooks.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                if (ids.contains(doc.getId())) {
                    Book book = doc.toObject(Book.class);
                    book.setId(doc.getId());
                    wishlistBooks.add(book);
                }
            }
            adapter.notifyDataSetChanged();
            binding.layoutEmptyWishlist.setVisibility(wishlistBooks.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (wishlistListener != null) wishlistListener.remove();
        binding = null;
    }
}
