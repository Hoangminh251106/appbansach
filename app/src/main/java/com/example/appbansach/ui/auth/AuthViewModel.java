package com.example.appbansach.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appbansach.data.repository.UserRepository;
import com.example.appbansach.model.User;
import com.example.appbansach.utils.Resource;

public class AuthViewModel extends ViewModel {
    private final UserRepository userRepository;
    
    private final MutableLiveData<Resource<User>> _loginStatus = new MutableLiveData<>();
    public LiveData<Resource<User>> getLoginStatus() { return _loginStatus; }

    private final MutableLiveData<Resource<Boolean>> _registerStatus = new MutableLiveData<>();
    public LiveData<Resource<Boolean>> getRegisterStatus() { return _registerStatus; }

    public AuthViewModel() {
        this.userRepository = new UserRepository();
    }

    public void login(String email, String password) {
        _loginStatus.setValue(Resource.loading(null));
        userRepository.login(email, password).observeForever(resource -> {
            _loginStatus.postValue(resource);
        });
    }

    public void register(String email, String password, String fullName) {
        _registerStatus.setValue(Resource.loading(null));
        userRepository.register(email, password, fullName).observeForever(resource -> {
            _registerStatus.postValue(resource);
        });
    }

    // Thêm hàm để reset trạng thái login
    public void resetLoginStatus() {
        _loginStatus.setValue(null);
    }

    public void logout() {
        userRepository.logout();
        resetLoginStatus();
    }
}
