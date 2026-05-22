package com.example.bookstore.ui.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.appbansach.R;
import com.example.bookstore.data.model.Book;
import com.example.bookstore.data.model.Category;
import com.example.appbansach.databinding.FragmentAddEditBookBinding;
import com.example.appbansach.utils.Resource;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class AddEditBookFragment extends Fragment {
    private FragmentAddEditBookBinding binding;
    private AdminViewModel viewModel;
    private Book currentBook;
    private Uri imageUri;
    private String selectedCategoryId;
    private List<Category> categories = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.ivAddBook.setImageURI(imageUri);
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentBook = (Book) getArguments().getSerializable("book");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEditBookBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        setupCategorySpinner();
        
        if (currentBook != null) {
            populateFields(currentBook);
        }

        binding.ivAddBook.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.btnSaveBook.setOnClickListener(v -> saveBook());

        viewModel.operationStatus.observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.btnSaveBook.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), resource.data, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveBook.setEnabled(true);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void setupCategorySpinner() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                categories = resource.data;
                List<String> names = new ArrayList<>();
                for (Category c : categories) names.add(c.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
                binding.spinnerCategory.setAdapter(adapter);
                
                binding.spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
                    selectedCategoryId = categories.get(position).getId();
                });

                if (currentBook != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getId().equals(currentBook.getCategoryId())) {
                            binding.spinnerCategory.setText(categories.get(i).getName(), false);
                            selectedCategoryId = categories.get(i).getId();
                            break;
                        }
                    }
                }
            }
        });
    }

    private void populateFields(Book book) {
        binding.etTitle.setText(book.getTitle());
        binding.etAuthor.setText(book.getAuthor());
        binding.etPrice.setText(String.valueOf(book.getPrice()));
        binding.etOriginalPrice.setText(String.valueOf(book.getOriginalPrice()));
        binding.etStock.setText(String.valueOf(book.getStock()));
        binding.etDescription.setText(book.getDescription());
        binding.switchFeatured.setChecked(book.isFeatured());
        binding.switchNew.setChecked(book.isNew());
        
        Glide.with(this).load(book.getImageUrl()).into(binding.ivAddBook);
    }

    private void saveBook() {
        String title = binding.etTitle.getText().toString().trim();
        String author = binding.etAuthor.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        String originalPriceStr = binding.etOriginalPrice.getText().toString().trim();
        String stockStr = binding.etStock.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author) || TextUtils.isEmpty(priceStr) || selectedCategoryId == null) {
            Toast.makeText(getContext(), "Vui lòng điền các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null && (currentBook == null || TextUtils.isEmpty(currentBook.getImageUrl()))) {
            Toast.makeText(getContext(), "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        Book book = (currentBook != null) ? currentBook : new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(Long.parseLong(priceStr));
        book.setOriginalPrice(TextUtils.isEmpty(originalPriceStr) ? 0 : Long.parseLong(originalPriceStr));
        book.setStock(Integer.parseInt(stockStr));
        book.setCategoryId(selectedCategoryId);
        book.setDescription(description);
        book.setFeatured(binding.switchFeatured.isChecked());
        book.setNew(binding.switchNew.isChecked());
        if (currentBook == null) {
            book.setCreatedAt(Timestamp.now());
        }

        viewModel.uploadImageAndSaveBook(book, imageUri, currentBook != null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
