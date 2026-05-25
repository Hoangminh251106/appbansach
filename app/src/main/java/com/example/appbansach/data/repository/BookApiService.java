package com.example.appbansach.data.repository;

import com.example.appbansach.data.model.GoogleBooksResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BookApiService {
    @GET("volumes")
    Call<GoogleBooksResponse> searchBooks(
            @Query("q") String query,
            @Query("key") String apiKey
    );
}
