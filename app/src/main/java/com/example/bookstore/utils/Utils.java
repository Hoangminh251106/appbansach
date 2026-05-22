package com.example.bookstore.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class Utils {
    public static String formatCurrency(long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}