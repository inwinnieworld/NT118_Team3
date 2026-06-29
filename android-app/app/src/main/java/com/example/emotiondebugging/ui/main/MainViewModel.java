package com.example.emotiondebugging.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.utils.SharedPrefsHelper;


public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> _fullName = new MutableLiveData<>();
    public LiveData<String> getFullName() {
        return _fullName;
    }

    private final MutableLiveData<int[]> _iconList = new MutableLiveData<>();
    public LiveData<int[]> getIconList() {
        return _iconList;
    }

    /**
     * Khởi tạo dữ liệu ban đầu theo đúng logic cũ
     */
    public void initData(SharedPrefsHelper prefsHelper) {
        String realName = prefsHelper.getName();
        _fullName.setValue(realName);

        int[] icons = {
                R.drawable.ic_errorlog,
                R.drawable.ic_emergencyhotfixes,
                R.drawable.ic_gitcommitjournal,
                R.drawable.ic_debuggingcommunity,
                R.drawable.ic_exammode,
                R.drawable.ic_heart
        };
        _iconList.setValue(icons);
    }
}
