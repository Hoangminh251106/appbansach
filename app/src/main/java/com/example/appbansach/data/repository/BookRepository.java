package com.example.appbansach.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.appbansach.BuildConfig;
import com.example.appbansach.data.model.GoogleBooksResponse;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.ReviewModel;
import com.example.appbansach.utils.Resource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final BookApiService apiService;
    
    private static final String API_KEY = BuildConfig.GOOGLE_BOOKS_API_KEY;

    public BookRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/books/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(BookApiService.class);
    }

    public LiveData<Resource<Book>> getBookDetails(String bookId) {
        MutableLiveData<Resource<Book>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("books").document(bookId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Book book = documentSnapshot.toObject(Book.class);
                if (book != null) {
                    book.setId(documentSnapshot.getId());
                    if (book.getStock() <= 0) book.setStock(50);
                    result.setValue(Resource.success(book));
                }
            } else {
                result.setValue(Resource.error("Không tìm thấy sách", null));
            }
        }).addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));
        
        return result;
    }

    public LiveData<Resource<List<ReviewModel>>> getReviews(String bookId) {
        MutableLiveData<Resource<List<ReviewModel>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("reviews")
                .whereEqualTo("bookId", bookId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        List<ReviewModel> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            reviews.add(doc.toObject(ReviewModel.class));
                        }
                        result.setValue(Resource.success(reviews));
                    }
                });
        
        return result;
    }

    public LiveData<Resource<Boolean>> submitReview(String bookId, ReviewModel review) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        String uid = FirebaseAuth.getInstance().getUid();
        review.setUserId(uid);
        review.setBookId(bookId);
        if (review.getCreatedAt() == null) review.setCreatedAt(Timestamp.now());

        DocumentReference bookRef = db.collection("books").document(bookId);
        
        db.collection("reviews").add(review).addOnSuccessListener(ref -> {
            db.runTransaction(transaction -> {
                com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(bookRef);
                if (snapshot.exists()) {
                    long currentCount = snapshot.getLong("reviewCount") != null ? snapshot.getLong("reviewCount") : 0;
                    double currentRating = snapshot.getDouble("rating") != null ? snapshot.getDouble("rating") : 0.0;
                    
                    long newCount = currentCount + 1;
                    double newRating = ((currentRating * currentCount) + review.getRating()) / newCount;
                    
                    transaction.update(bookRef, "reviewCount", newCount);
                    transaction.update(bookRef, "rating", newRating);
                }
                return null;
            }).addOnSuccessListener(aVoid -> result.setValue(Resource.success(true)))
              .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), false)));
        });
        
        return result;
    }

    public LiveData<Resource<List<Book>>> fetchBooksFromApi(String query) {
        MutableLiveData<Resource<List<Book>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        apiService.searchBooks(query, API_KEY).enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(Call<GoogleBooksResponse> call, Response<GoogleBooksResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = new ArrayList<>();
                    if (response.body().getItems() != null) {
                        for (GoogleBooksResponse.Item item : response.body().getItems()) {
                            books.add(convertToBook(item));
                        }
                    }
                    result.setValue(Resource.success(books));
                } else {
                    result.setValue(Resource.error("Lỗi khi tải dữ liệu", null));
                }
            }
            @Override public void onFailure(Call<GoogleBooksResponse> call, Throwable t) {
                result.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return result;
    }

    /**
     * Tìm kiếm sách theo danh mục chính xác hơn bằng cách sử dụng prefix subject:
     */
    public LiveData<Resource<List<Book>>> fetchBooksByCategory(String categoryName) {
        String query = "subject:\"" + categoryName + "\"";
        return fetchBooksFromApi(query);
    }

    private Book convertToBook(GoogleBooksResponse.Item item) {
        Book book = new Book();
        book.setId(item.getId());
        GoogleBooksResponse.VolumeInfo info = item.getVolumeInfo();
        if (info != null) {
            book.setTitle(info.getTitle() != null ? info.getTitle() : "N/A");
            book.setAuthor(info.getAuthors() != null && !info.getAuthors().isEmpty() ? info.getAuthors().get(0) : "Unknown");
            if (info.getImageLinks() != null) {
                String url = info.getImageLinks().getThumbnail();
                if (url != null) book.setImageUrl(url.replace("http://", "https://"));
            }
            if (info.getDescription() != null) book.setDescription(info.getDescription());
        }
        book.setStock(99);
        if (item.getSaleInfo() != null && item.getSaleInfo().getListPrice() != null) {
            book.setPrice((long) item.getSaleInfo().getListPrice().getAmount());
        } else {
            book.setPrice(85000 + (long)(Math.random() * 150000));
        }
        return book;
    }

    public LiveData<Resource<List<Book>>> getFeaturedBooks() {
        MutableLiveData<Resource<List<Book>>> result = new MutableLiveData<>();
        db.collection("books").limit(10).get().addOnSuccessListener(value -> {
            List<Book> books = new ArrayList<>();
            for (QueryDocumentSnapshot document : value) {
                Book book = document.toObject(Book.class);
                book.setId(document.getId());
                books.add(book);
            }
            result.setValue(Resource.success(books));
        });
        return result;
    }
}
