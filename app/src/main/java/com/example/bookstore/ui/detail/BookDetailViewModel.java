package com.example.bookstore.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookstore.data.local.CartDao;
import com.example.bookstore.data.local.CartDatabase;
import com.example.bookstore.data.local.CartItemEntity;
import com.example.bookstore.data.model.Book;
import com.example.bookstore.data.model.Review;
import com.example.bookstore.data.repository.BookRepository;
import com.example.appbansach.utils.Resource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookDetailViewModel extends AndroidViewModel {
    private final BookRepository bookRepository;
    private final CartDao cartDao;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<Resource<List<Review>>> _reviews = new MutableLiveData<>();
    public LiveData<Resource<List<Review>>> reviews = _reviews;

    public BookDetailViewModel(@NonNull Application application) {
        super(application);
        this.bookRepository = new BookRepository();
        this.cartDao = CartDatabase.getDatabase(application).cartDao();
    }

    public LiveData<Resource<Book>> getBookById(String bookId) {
        return bookRepository.getBookById(bookId);
    }

    public void loadReviews(String bookId) {
        _reviews.setValue(Resource.loading(null));
        db.collection("books").document(bookId).collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviewList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Review review = doc.toObject(Review.class);
                            review.setReviewId(doc.getId());
                            reviewList.add(review);
                        }
                        _reviews.setValue(Resource.success(reviewList));
                    } else {
                        _reviews.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
    }

    public void addToCart(Book book) {
        CartDatabase.databaseWriteExecutor.execute(() -> {
            CartItemEntity existingItem = cartDao.getItemByBookId(book.getId());
            if (existingItem != null) {
                cartDao.updateQuantity(book.getId(), existingItem.getQuantity() + 1);
            } else {
                CartItemEntity newItem = new CartItemEntity(
                        book.getId(),
                        book.getTitle(),
                        book.getImageUrl(),
                        book.getPrice(),
                        1
                );
                cartDao.insertOrUpdate(newItem);
            }
        });
    }
}