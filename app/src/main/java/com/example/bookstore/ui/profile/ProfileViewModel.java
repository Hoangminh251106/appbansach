package com.example.bookstore.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookstore.data.model.User;
import com.example.bookstore.data.repository.UserRepository;
import com.example.appbansach.utils.Resource;

public class ProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Resource<User>> _userProfile = new MutableLiveData<>();
    public LiveData<Resource<User>> userProfile = _userProfile;

    public ProfileViewModel() {
        this.userRepository = new UserRepository();
        loadUserProfile();
    }

    public void loadUserProfile() {
        String uid = userRepository.getCurrentUid();
        if (uid != null) {
            userRepository.getUserDetails(uid, _userProfile);
        } else {
            _userProfile.setValue(Resource.error("User not logged in", null));
        }
    }

    public void logout() {
        userRepository.logout();
    }
}