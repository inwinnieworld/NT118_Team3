package com.example.emotiondebugging.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.emotiondebugging.R;

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
    public void initData() {
        // Giả lập tên lấy từ Database/SharedPrefs
        _fullName.setValue("Trương Nguyên Đại Thắng");

        // Mảng chứa ID của 5 file PNG icon chức năng
        int[] icons = {
                R.drawable.ic_errorlog,
                R.drawable.ic_emergencyhotfixes,
                R.drawable.ic_gitcommitjournal,
                R.drawable.ic_debuggingcommunity,
                R.drawable.ic_exammode
        };
        _iconList.setValue(icons);
    }
}