package com.example.bookstore.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookstore.data.model.Banner;
import com.example.bookstore.data.model.Book;
import com.example.bookstore.data.model.Category;
import com.example.bookstore.data.repository.BannerRepository;
import com.example.bookstore.data.repository.BookRepository;
import com.example.bookstore.data.repository.CategoryRepository;
import com.example.appbansach.utils.Resource;

import java.util.List;

public class HomeViewModel extends ViewModel {
    private final BannerRepository bannerRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public HomeViewModel() {
        this.bannerRepository = new BannerRepository();
        this.categoryRepository = new CategoryRepository();
        this.bookRepository = new BookRepository();
    }

    public LiveData<Resource<List<Banner>>> getBanners() {
        return bannerRepository.getBanners();
    }

    public LiveData<Resource<List<Category>>> getCategories() {
        return categoryRepository.getCategories();
    }

    public LiveData<Resource<List<Book>>> getFeaturedBooks() {
        return bookRepository.getFeaturedBooks();
    }

    public LiveData<Resource<List<Book>>> getNewestBooks() {
        return bookRepository.getNewestBooks();
    }
}