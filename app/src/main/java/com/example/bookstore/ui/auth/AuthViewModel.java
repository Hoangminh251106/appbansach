package com.example.bookstore.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookstore.data.repository.UserRepository;
import com.example.bookstore.data.model.User;
import com.example.bookstore.utils.Resource;

public class AuthViewModel extends ViewModel {
    private final UserRepository userRepository;

    private final MutableLiveData<Resource<User>> _authStatus = new MutableLiveData<>();
    public LiveData<Resource<User>> getAuthStatus() { return _authStatus; }

    private final MutableLiveData<Resource<Boolean>> _registerStatus = new MutableLiveData<>();
    public LiveData<Resource<Boolean>> getRegisterStatus() { return _registerStatus; }

    public AuthViewModel() {
        this.userRepository = new UserRepository();
    }

    public void login(String email, String password) {
        _authStatus.setValue(Resource.loading(null));
        // Gán trực tiếp kết quả từ Repository vào LiveData
        userRepository.login(email, password).observeForever(resource -> {
            _authStatus.postValue(resource);
        });
    }

    public void register(String email, String password, String fullName) {
        _registerStatus.setValue(Resource.loading(null));
        userRepository.register(email, password, fullName).observeForever(resource -> {
            _registerStatus.postValue(resource);
        });
    }

    public void logout() {
        userRepository.logout();
    }

    public String getCurrentUid() {
        return userRepository.getCurrentUid();
    }
}
