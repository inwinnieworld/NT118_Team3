package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.StaffRepository;
import com.example.emotiondebugging.model.response.QuestMonthlyMetricResponse;
import com.example.emotiondebugging.model.response.QuestRankingBoardResponse;

import java.util.List;

public class QuestReportViewModel extends ViewModel {

    private final StaffRepository repository = new StaffRepository();

    private final MutableLiveData<List<QuestMonthlyMetricResponse>> monthlyMetrics = new MutableLiveData<>();
    private final MutableLiveData<List<QuestRankingBoardResponse>> rankingBoard = new MutableLiveData<>();
    private final MutableLiveData<String> selectedMetric = new MutableLiveData<>("MỨC ĐỘ NGHIÊM TRỌNG TB");
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<QuestMonthlyMetricResponse>> getMonthlyMetrics() {
        return monthlyMetrics;
    }

    public LiveData<List<QuestRankingBoardResponse>> getRankingBoard() {
        return rankingBoard;
    }

    public LiveData<String> getSelectedMetric() {
        return selectedMetric;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void setSelectedMetric(String metric) {
        selectedMetric.setValue(metric);
    }

    public void loadMonthlyMetrics() {
        repository.getQuestMonthlyMetrics(monthlyMetrics, message, loading);
    }

    public void loadRankingBoard() {
        repository.getQuestRankingBoard(rankingBoard, message, loading);
    }
}