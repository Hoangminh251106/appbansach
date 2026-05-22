package com.example.bookstore.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookstore.data.model.Book;
import com.example.bookstore.data.model.Category;
import com.example.bookstore.data.repository.BookRepository;
import com.example.bookstore.data.repository.CategoryRepository;
import com.example.appbansach.utils.Resource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchViewModel extends ViewModel {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    
    private final MutableLiveData<Resource<List<Book>>> _searchResults = new MutableLiveData<>();
    public LiveData<Resource<List<Book>>> searchResults = _searchResults;

    private List<Book> allBooksBuffer = new ArrayList<>();
    private String currentQuery = "";
    private String currentCategoryId = null;
    private float minPrice = 0;
    private float maxPrice = Float.MAX_VALUE;

    public SearchViewModel() {
        this.bookRepository = new BookRepository();
        this.categoryRepository = new CategoryRepository();
        loadAllBooks();
    }

    public LiveData<Resource<List<Category>>> getCategories() {
        return categoryRepository.getCategories();
    }

    public void loadAllBooks() {
        _searchResults.setValue(Resource.loading(null));
        db.collection("books").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Book> books = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Book book = document.toObject(Book.class);
                    book.setId(document.getId());
                    books.add(book);
                }
                allBooksBuffer = books;
                applyFilters();
            } else {
                _searchResults.setValue(Resource.error(task.getException().getMessage(), null));
            }
        });
    }

    public void search(String query) {
        this.currentQuery = query.toLowerCase();
        applyFilters();
    }

    public void filterByCategory(String categoryId) {
        this.currentCategoryId = categoryId;
        applyFilters();
    }

    public void filterByPrice(float min, float max) {
        this.minPrice = min;
        this.maxPrice = max;
        applyFilters();
    }

    private void applyFilters() {
        List<Book> filtered = allBooksBuffer.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(currentQuery) || 
                                book.getAuthor().toLowerCase().contains(currentQuery))
                .filter(book -> currentCategoryId == null || book.getCategoryId().equals(currentCategoryId))
                .filter(book -> book.getPrice() >= minPrice && book.getPrice() <= maxPrice)
                .collect(Collectors.toList());
        
        _searchResults.setValue(Resource.success(filtered));
    }
}