package com.example.bookstore.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookstore.data.model.Banner;
import com.example.appbansach.utils.Resource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BannerRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Resource<List<Banner>>> getBanners() {
        MutableLiveData<Resource<List<Banner>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection("banners")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Banner> banners = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Banner banner = document.toObject(Banner.class);
                            banner.setId(document.getId());
                            banners.add(banner);
                        }
                        result.setValue(Resource.success(banners));
                    } else {
                        result.setValue(Resource.error(task.getException().getMessage(), null));
                    }
                });
        return result;
    }
}
