package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.StaffRepository;
import com.example.emotiondebugging.model.response.QuestRankingReportResponse;
import com.example.emotiondebugging.model.response.QuestSummaryReportResponse;
import com.example.emotiondebugging.model.response.QuestTrendReportResponse;

import java.util.List;

public class QuestReportViewModel extends ViewModel {

    private final StaffRepository repository = new StaffRepository();

    private final MutableLiveData<QuestSummaryReportResponse> summaryReport = new MutableLiveData<>();
    private final MutableLiveData<List<QuestRankingReportResponse>> rankingReport = new MutableLiveData<>();
    private final MutableLiveData<QuestTrendReportResponse> trendReport = new MutableLiveData<>();
    private final MutableLiveData<String> selectedMetric = new MutableLiveData<>("MỨC ĐỘ NGHIÊM TRỌNG TB");
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<QuestSummaryReportResponse> getSummaryReport() {
        return summaryReport;
    }

    public LiveData<List<QuestRankingReportResponse>> getRankingReport() {
        return rankingReport;
    }

    public LiveData<QuestTrendReportResponse> getTrendReport() {
        return trendReport;
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

    public void loadSummaryReport() {
        repository.getQuestSummaryReport(summaryReport, message, loading);
    }

    public void loadRankingReport() {
        repository.getQuestRankingReport(rankingReport, message, loading);
    }

    public void loadTrendReport() {
        repository.getQuestTrendReport(trendReport, message, loading);
    }
}