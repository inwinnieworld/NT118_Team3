package com.example.emotiondebugging.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

public class AdminViewModel extends ViewModel {

    private final MutableLiveData<String> _adminName = new MutableLiveData<>();
    public LiveData<String> getAdminName() {
        return _adminName;
    }

    public void loadAdminData(SharedPrefsHelper prefsHelper) {
        // Lấy tên thật từ lúc Login
        String name = prefsHelper.getName();
        _adminName.setValue(name);
    }
}