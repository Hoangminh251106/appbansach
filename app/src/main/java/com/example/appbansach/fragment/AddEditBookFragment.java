package com.example.appbansach.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.databinding.FragmentAddEditBookBinding;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.Category;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AddEditBookFragment extends Fragment {
    private FragmentAddEditBookBinding binding;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String bookId;
    private Book currentBook;
    private Uri imageUri;
    private List<Category> categories = new ArrayList<>();
    private String selectedCategoryId;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.ivAddBook.setImageURI(uri);
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
        }

        loadCategories();

        if (bookId != null) {
            loadBookData();
        }

        binding.cardBookImage.setOnClickListener(v -> getContent.launch("image/*"));
        binding.btnSaveBook.setOnClickListener(v -> saveBook());

        return binding.getRoot();
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!isAdded()) return;
            categories.clear();
            List<String> catNames = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                cat.setId(doc.getId());
                categories.add(cat);
                catNames.add(cat.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, catNames);
            binding.spinnerCategory.setAdapter(adapter);
            binding.spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
                selectedCategoryId = categories.get(position).getId();
            });
        });
    }

    private void loadBookData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("books").document(bookId).get().addOnSuccessListener(documentSnapshot -> {
            if (!isAdded()) return;
            binding.progressBar.setVisibility(View.GONE);
            currentBook = documentSnapshot.toObject(Book.class);
            if (currentBook != null) {
                currentBook.setId(documentSnapshot.getId());
                binding.etTitle.setText(currentBook.getTitle());
                binding.etAuthor.setText(currentBook.getAuthor());
                binding.etPrice.setText(String.valueOf(currentBook.getPrice()));
                binding.etOriginalPrice.setText(String.valueOf(currentBook.getOriginalPrice()));
                binding.etStock.setText(String.valueOf(currentBook.getStock()));
                binding.etDescription.setText(currentBook.getDescription());
                binding.switchFeatured.setChecked(currentBook.isFeatured());
                binding.switchNew.setChecked(currentBook.isNew());
                selectedCategoryId = currentBook.getCategoryId();
                
                // Hiển thị tên danh mục hiện tại
                for (Category cat : categories) {
                    if (cat.getId().equals(selectedCategoryId)) {
                        binding.spinnerCategory.setText(cat.getName(), false);
                        break;
                    }
                }

                Glide.with(this).load(currentBook.getImageUrl()).into(binding.ivAddBook);
            }
        });
    }

    private void saveBook() {
        String title = binding.etTitle.getText().toString().trim();
        String author = binding.etAuthor.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        String stockStr = binding.etStock.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || selectedCategoryId == null) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveBook.setEnabled(false);

        if (imageUri != null) {
            uploadImageAndSaveBook(title, author, Long.parseLong(priceStr), Integer.parseInt(stockStr), desc);
        } else if (currentBook != null) {
            saveToFirestore(currentBook.getImageUrl(), title, author, Long.parseLong(priceStr), Integer.parseInt(stockStr), desc);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSaveBook.setEnabled(true);
            Toast.makeText(getContext(), "Vui lòng chọn ảnh bìa", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveBook(String title, String author, long price, int stock, String desc) {
        String fileName = "books/" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
            saveToFirestore(uri.toString(), title, author, price, stock, desc);
        })).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSaveBook.setEnabled(true);
            Toast.makeText(getContext(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveToFirestore(String imageUrl, String title, String author, long price, int stock, String desc) {
        Book book = currentBook != null ? currentBook : new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        book.setImageUrl(imageUrl);
        book.setStock(stock);
        book.setDescription(desc);
        book.setCategoryId(selectedCategoryId);
        book.setFeatured(binding.switchFeatured.isChecked());
        book.setNew(binding.switchNew.isChecked());
        
        String origPrice = binding.etOriginalPrice.getText().toString().trim();
        if (!origPrice.isEmpty()) book.setOriginalPrice(Long.parseLong(origPrice));

        if (book.getCreatedAt() == null) book.setCreatedAt(Timestamp.now());

        if (bookId != null) {
            db.collection("books").document(bookId).set(book)
                    .addOnSuccessListener(v -> onSaveSuccess())
                    .addOnFailureListener(e -> onSaveFailure(e));
        } else {
            db.collection("books").add(book)
                    .addOnSuccessListener(v -> onSaveSuccess())
                    .addOnFailureListener(e -> onSaveFailure(e));
        }
    }

    private void onSaveSuccess() {
        Toast.makeText(getContext(), "Đã lưu thông tin sách", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void onSaveFailure(Exception e) {
        binding.progressBar.setVisibility(View.GONE);
        binding.btnSaveBook.setEnabled(true);
        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
