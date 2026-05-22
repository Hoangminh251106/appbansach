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
    public LiveData<Resource<User>> loginStatus = _loginStatus;

    private final MutableLiveData<Resource<Boolean>> _registerStatus = new MutableLiveData<>();
    public LiveData<Resource<Boolean>> registerStatus = _registerStatus;

    public AuthViewModel() {
        this.userRepository = new UserRepository();
    }

    public void login(String email, String password) {
        userRepository.login(email, password).observeForever(_loginStatus::setValue);
    }

    public void register(String email, String password, String fullName) {
        userRepository.register(email, password, fullName).observeForever(_registerStatus::setValue);
    }
}