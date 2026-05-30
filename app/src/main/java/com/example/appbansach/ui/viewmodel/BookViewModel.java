package com.example.appbansach.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appbansach.data.repository.BookRepository;
import com.example.appbansach.model.Book;
import com.example.appbansach.model.ReviewModel;
import com.example.appbansach.utils.Resource;

import java.util.List;

public class BookViewModel extends ViewModel {
    private final BookRepository bookRepository;

    private final MutableLiveData<Resource<List<Book>>> _featuredBooks = new MutableLiveData<>();
    public LiveData<Resource<List<Book>>> getFeaturedBooks() { return _featuredBooks; }

    private final MutableLiveData<Resource<List<Book>>> _bestSellingBooks = new MutableLiveData<>();
    public LiveData<Resource<List<Book>>> getBestSellingBooks() { return _bestSellingBooks; }

    private final MutableLiveData<Resource<List<Book>>> _mostLikedBooks = new MutableLiveData<>();
    public LiveData<Resource<List<Book>>> getMostLikedBooks() { return _mostLikedBooks; }

    private final MutableLiveData<Resource<List<Book>>> _newestBooks = new MutableLiveData<>();
    public LiveData<Resource<List<Book>>> getNewestBooks() { return _newestBooks; }

    private final MutableLiveData<Resource<List<Book>>> _searchResults = new MutableLiveData<>();
    public LiveData<Resource<List<Book>>> getSearchResults() { return _searchResults; }

    private final MutableLiveData<Resource<Book>> _bookDetails = new MutableLiveData<>();
    public LiveData<Resource<Book>> getBookDetails() { return _bookDetails; }

    private final MutableLiveData<Resource<List<ReviewModel>>> _reviews = new MutableLiveData<>();
    public LiveData<Resource<List<ReviewModel>>> getReviews() { return _reviews; }

    private final MutableLiveData<Resource<Boolean>> _reviewSubmitStatus = new MutableLiveData<>();
    public LiveData<Resource<Boolean>> getReviewSubmitStatus() { return _reviewSubmitStatus; }

    public BookViewModel() {
        this.bookRepository = new BookRepository();
    }

    public void fetchFeaturedBooks() {
        bookRepository.fetchBooksFromApi("văn học Việt Nam").observeForever(_featuredBooks::postValue);
    }

    public void fetchBestSellingBooks() {
        bookRepository.fetchBooksFromApi("kinh tế").observeForever(_bestSellingBooks::postValue);
    }

    public void fetchMostLikedBooks() {
        bookRepository.fetchBooksFromApi("kỹ năng sống").observeForever(_mostLikedBooks::postValue);
    }

    public void fetchNewestBooks() {
        bookRepository.fetchBooksFromApi("sách mới").observeForever(_newestBooks::postValue);
    }

    public void searchBooks(String query) {
        bookRepository.fetchBooksFromApi(query).observeForever(_searchResults::postValue);
    }

    public void searchByCategory(String categoryName) {
        bookRepository.fetchBooksByCategory(categoryName).observeForever(_searchResults::postValue);
    }

    public void fetchBookDetails(String bookId) {
        bookRepository.getBookDetails(bookId).observeForever(_bookDetails::postValue);
    }

    public void fetchReviews(String bookId) {
        bookRepository.getReviews(bookId).observeForever(_reviews::postValue);
    }

    public void submitReview(String bookId, ReviewModel review) {
        bookRepository.submitReview(bookId, review).observeForever(_reviewSubmitStatus::postValue);
    }
}
