package com.example.bookstore.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookstore.data.model.Book;
import com.example.appbansach.utils.Resource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Resource<List<Book>>> getFeaturedBooks() {
        MutableLiveData<Resource<List<Book>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("books")
                .whereEqualTo("isFeatured", true)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Book> books = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            books.add(book);
                        }
                        result.setValue(Resource.success(books));
                    } else {
                        result.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
        return result;
    }

    public LiveData<Resource<List<Book>>> getNewestBooks() {
        MutableLiveData<Resource<List<Book>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("books")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Book> books = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            books.add(book);
                        }
                        result.setValue(Resource.success(books));
                    } else {
                        result.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
        return result;
    }

    public LiveData<Resource<Book>> getBookById(String bookId) {
        MutableLiveData<Resource<Book>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("books").document(bookId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Book book = documentSnapshot.toObject(Book.class);
                        book.setId(documentSnapshot.getId());
                        result.setValue(Resource.success(book));
                    } else {
                        result.setValue(Resource.error("Không tìm thấy sách", null));
                    }
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));
        return result;
    }

    public LiveData<Resource<List<Book>>> searchBooks(String query) {
        MutableLiveData<Resource<List<Book>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("books")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Book> books = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            books.add(book);
                        }
                        result.setValue(Resource.success(books));
                    } else {
                        result.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
        return result;
    }
}
