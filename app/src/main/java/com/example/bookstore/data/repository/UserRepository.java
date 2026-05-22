package com.example.bookstore.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookstore.data.model.User;
import com.example.bookstore.utils.Resource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Resource<Boolean>> register(String email, String password, String fullName) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        User user = new User(uid, fullName, email, "", "", "customer", Timestamp.now());
                        saveUserToFirestore(user, result);
                    } else {
                        String error = "Đăng ký thất bại";
                        if (task.getException() instanceof FirebaseAuthException) {
                            error = task.getException().getLocalizedMessage();
                        }
                        result.setValue(Resource.error(error, null));
                    }
                });
        return result;
    }

    private void saveUserToFirestore(User user, MutableLiveData<Resource<Boolean>> result) {
        db.collection("users").document(user.getUid()).set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        result.setValue(Resource.success(true));
                    } else {
                        result.setValue(Resource.error("Lỗi lưu database: " + task.getException().getMessage(), null));
                    }
                });
    }

    public LiveData<Resource<User>> login(String email, String password) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        getUserDetails(task.getResult().getUser().getUid(), result);
                    } else {
                        result.setValue(Resource.error("Sai tài khoản hoặc mật khẩu", null));
                    }
                });
        return result;
    }

    public void getUserDetails(String uid, MutableLiveData<Resource<User>> result) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        result.setValue(Resource.success(user));
                    } else {
                        result.setValue(Resource.error("Không tìm thấy thông tin người dùng", null));
                    }
                })
                .addOnFailureListener(e -> result.setValue(Resource.error(e.getMessage(), null)));
    }

    public void logout() {
        mAuth.signOut();
    }

    public String getCurrentUid() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }
}
