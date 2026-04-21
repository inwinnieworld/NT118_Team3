package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

public class StaffViewModel extends ViewModel {

    private final MutableLiveData<String> _staffName = new MutableLiveData<>();
    public LiveData<String> getStaffName() {
        return _staffName;
    }

    public void loadStaffData(SharedPrefsHelper prefsHelper) {
        // Lấy tên thật từ lúc Login
        String name = prefsHelper.getName();
        _staffName.setValue(name);
    }
}