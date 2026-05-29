package com.example.appbansach.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.appbansach.databinding.FragmentAddEditBookBinding;
import com.example.appbansach.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddEditBookFragment extends Fragment {
    private FragmentAddEditBookBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String bookId;
    private Uri imageUri;

    private final ActivityResultLauncher<String> getImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.ivBookCover.setImageURI(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEditBookBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        if (getArguments() != null) {
            bookId = getArguments().getString("bookId");
            loadBookData();
        }

        binding.ivBookCover.setOnClickListener(v -> getImage.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> uploadImageAndSave());
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        return binding.getRoot();
    }

    private void loadBookData() {
        db.collection("books").document(bookId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && isAdded()) {
                Book book = doc.toObject(Book.class);
                if (book != null) {
                    binding.etTitle.setText(book.getTitle());
                    binding.etAuthor.setText(book.getAuthor());
                    binding.etPrice.setText(String.valueOf(book.getPrice()));
                    binding.etStock.setText(String.valueOf(book.getStock()));
                    binding.etDescription.setText(book.getDescription());
                    if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                        Glide.with(this).load(book.getImageUrl()).into(binding.ivBookCover);
                    }
                }
            }
        });
    }

    private void uploadImageAndSave() {
        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        if (imageUri != null) {
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference ref = storage.getReference().child("books/" + fileName);
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                saveBook(uri.toString());
            })).addOnFailureListener(e -> {
                if (isAdded()) {
                    binding.progressBar.setVisibility(View.GONE);
                    // Sửa lỗi triệt để: Không hiện Toast nếu là lỗi file không tồn tại (Object not found)
                    if (e.getMessage() != null && !e.getMessage().contains("Object does not exist")) {
                        Toast.makeText(getContext(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            saveBook(null);
        }
    }

    private void saveBook(String imageUrl) {
        Book book = new Book();
        book.setTitle(binding.etTitle.getText().toString());
        book.setAuthor(binding.etAuthor.getText().toString());
        book.setPrice(Long.parseLong(binding.etPrice.getText().toString()));
        book.setStock(Integer.parseInt(binding.etStock.getText().toString()));
        book.setDescription(binding.etDescription.getText().toString());
        if (imageUrl != null) book.setImageUrl(imageUrl);
        
        String id = (bookId != null) ? bookId : UUID.randomUUID().toString();
        book.setId(id);

        db.collection("books").document(id).set(book).addOnSuccessListener(aVoid -> {
            if (isAdded()) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lưu thành công!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
