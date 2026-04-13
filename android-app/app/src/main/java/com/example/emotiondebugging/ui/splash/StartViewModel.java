package com.example.emotiondebugging.ui.splash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.ui.auth.LoginActivity;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

public class StartViewModel extends ViewModel {

    private final MutableLiveData<Class<?>> _navigationTarget = new MutableLiveData<>();
    public LiveData<Class<?>> getNavigationTarget() {
        return _navigationTarget;
    }

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> getToastMessage() {
        return _toastMessage;
    }

    public void checkNavigation(SharedPrefsHelper prefsHelper) {
        if (prefsHelper.isLoggedIn() && prefsHelper.getToken() != null) {
            String savedRole = prefsHelper.getRole();
            if (savedRole == null) savedRole = "STUDENT";

            switch (savedRole.toUpperCase()) {
                case "ADMIN":
                    _navigationTarget.setValue(com.example.emotiondebugging.ui.admin.AdminDashboardActivity.class);
                    break;
                case "STAFF":
                    _navigationTarget.setValue(com.example.emotiondebugging.ui.staff.StaffDashboardActivity.class);
                    break;
                default:
                    _navigationTarget.setValue(com.example.emotiondebugging.ui.main.MainActivity.class);
                    break;
            }
        } else {
            if (prefsHelper.getEmail() != null && prefsHelper.getToken() == null) {
                _toastMessage.setValue("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
            }
            _navigationTarget.setValue(LoginActivity.class);
        }
    }
}