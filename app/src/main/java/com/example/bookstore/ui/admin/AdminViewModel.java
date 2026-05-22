package com.example.bookstore.ui.admin;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookstore.data.model.Book;
import com.example.bookstore.data.model.Category;
import com.example.bookstore.data.model.Order;
import com.example.bookstore.data.repository.BookRepository;
import com.example.bookstore.data.repository.CategoryRepository;
import com.example.appbansach.utils.Resource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminViewModel extends ViewModel {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private final MutableLiveData<Resource<List<Book>>> _allBooks = new MutableLiveData<>();
    public LiveData<Resource<List<Book>>> allBooks = _allBooks;

    private final MutableLiveData<Resource<List<Order>>> _allOrders = new MutableLiveData<>();
    public LiveData<Resource<List<Order>>> allOrders = _allOrders;

    private final MutableLiveData<Resource<String>> _operationStatus = new MutableLiveData<>();
    public LiveData<Resource<String>> operationStatus = _operationStatus;

    public AdminViewModel() {
        this.bookRepository = new BookRepository();
        this.categoryRepository = new CategoryRepository();
    }

    public void loadAllBooks() {
        _allBooks.setValue(Resource.loading(null));
        db.collection("books")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Book> books = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            books.add(book);
                        }
                        _allBooks.setValue(Resource.success(books));
                    } else {
                        _allBooks.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
    }

    public void deleteBook(String bookId) {
        _operationStatus.setValue(Resource.loading(null));
        db.collection("books").document(bookId).delete()
                .addOnSuccessListener(aVoid -> {
                    _operationStatus.setValue(Resource.success("Xóa sách thành công"));
                    loadAllBooks();
                })
                .addOnFailureListener(e -> _operationStatus.setValue(Resource.error(e.getMessage(), null)));
    }

    public void uploadImageAndSaveBook(Book book, Uri imageUri, boolean isEdit) {
        _operationStatus.setValue(Resource.loading(null));
        if (imageUri != null) {
            StorageReference ref = storage.getReference().child("books/" + UUID.randomUUID().toString());
            ref.putFile(imageUri)
                    .continueWithTask(task -> ref.getDownloadUrl())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            book.setImageUrl(task.getResult().toString());
                            saveBookToFirestore(book, isEdit);
                        } else {
                            _operationStatus.setValue(Resource.error("Lỗi upload ảnh: " + task.getException().getMessage(), null));
                        }
                    });
        } else {
            saveBookToFirestore(book, isEdit);
        }
    }

    private void saveBookToFirestore(Book book, boolean isEdit) {
        if (isEdit) {
            db.collection("books").document(book.getId()).set(book)
                    .addOnSuccessListener(aVoid -> _operationStatus.setValue(Resource.success("Cập nhật sách thành công")))
                    .addOnFailureListener(e -> _operationStatus.setValue(Resource.error(e.getMessage(), null)));
        } else {
            db.collection("books").add(book)
                    .addOnSuccessListener(documentReference -> _operationStatus.setValue(Resource.success("Thêm sách thành công")))
                    .addOnFailureListener(e -> _operationStatus.setValue(Resource.error(e.getMessage(), null)));
        }
    }

    public LiveData<Resource<List<Category>>> getCategories() {
        return categoryRepository.getCategories();
    }

    public void loadAllOrders() {
        _allOrders.setValue(Resource.loading(null));
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            order.setOrderId(document.getId());
                            orders.add(order);
                        }
                        _allOrders.setValue(Resource.success(orders));
                    } else {
                        _allOrders.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        _operationStatus.setValue(Resource.loading(null));
        db.collection("orders").document(orderId).update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    _operationStatus.setValue(Resource.success("Cập nhật trạng thái thành công"));
                    loadAllOrders();
                })
                .addOnFailureListener(e -> _operationStatus.setValue(Resource.error(e.getMessage(), null)));
    }
}
