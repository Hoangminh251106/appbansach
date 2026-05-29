package com.example.appbansach.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {CartItemEntity.class}, version = 3, exportSchema = false)
public abstract class CartDatabase extends RoomDatabase {
    public abstract CartDao cartDao();

    private static volatile CartDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static CartDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CartDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    CartDatabase.class, "cart_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
