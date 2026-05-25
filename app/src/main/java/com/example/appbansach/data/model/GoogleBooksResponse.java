package com.example.appbansach.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GoogleBooksResponse {
    @SerializedName("items")
    private List<Item> items;

    public List<Item> getItems() { return items; }

    public static class Item {
        @SerializedName("id")
        private String id;
        @SerializedName("volumeInfo")
        private VolumeInfo volumeInfo;
        @SerializedName("saleInfo")
        private SaleInfo saleInfo;

        public String getId() { return id; }
        public VolumeInfo getVolumeInfo() { return volumeInfo; }
        public SaleInfo getSaleInfo() { return saleInfo; }
    }

    public static class VolumeInfo {
        @SerializedName("title")
        private String title;
        @SerializedName("authors")
        private List<String> authors;
        @SerializedName("description")
        private String description;
        @SerializedName("imageLinks")
        private ImageLinks imageLinks;
        @SerializedName("categories")
        private List<String> categories;

        public String getTitle() { return title; }
        public List<String> getAuthors() { return authors; }
        public String getDescription() { return description; }
        public ImageLinks getImageLinks() { return imageLinks; }
        public List<String> getCategories() { return categories; }
    }

    public static class ImageLinks {
        @SerializedName("thumbnail")
        private String thumbnail;

        public String getThumbnail() { return thumbnail; }
    }

    public static class SaleInfo {
        @SerializedName("listPrice")
        private ListPrice listPrice;

        public ListPrice getListPrice() { return listPrice; }
    }

    public static class ListPrice {
        @SerializedName("amount")
        private double amount;
        @SerializedName("currencyCode")
        private String currencyCode;

        public double getAmount() { return amount; }
        public String getCurrencyCode() { return currencyCode; }
    }
}
