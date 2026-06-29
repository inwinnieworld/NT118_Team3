package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.QuestBuilderRepository;
import com.example.emotiondebugging.model.response.QuestMonthlyMetricResponse;
import com.example.emotiondebugging.model.response.QuestRankingBoardResponse;
import com.example.emotiondebugging.model.response.QuestNodeMetricResponse;

import java.util.List;

public class QuestReportViewModel extends ViewModel {
    public static final String METRIC_RUNS = "TOTAL RUNS";
    public static final String METRIC_COMPLETION = "COMPLETION RATE (%)";
    public static final String METRIC_ABANDONMENT = "ABANDONMENT RATE (%)";
    public static final String METRIC_DURATION = "AVERAGE DURATION (MIN)";

    private final QuestBuilderRepository repository = new QuestBuilderRepository();
    private final MutableLiveData<List<QuestMonthlyMetricResponse>> monthlyMetrics = new MutableLiveData<>();
    private final MutableLiveData<List<QuestRankingBoardResponse>> rankingBoard = new MutableLiveData<>();
    private final MutableLiveData<List<QuestNodeMetricResponse>> nodeMetrics = new MutableLiveData<>();
    private final MutableLiveData<String> selectedMetric = new MutableLiveData<>(METRIC_RUNS);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<QuestMonthlyMetricResponse>> getMonthlyMetrics() { return monthlyMetrics; }
    public LiveData<List<QuestRankingBoardResponse>> getRankingBoard() { return rankingBoard; }
    public LiveData<List<QuestNodeMetricResponse>> getNodeMetrics() { return nodeMetrics; }
    public LiveData<String> getSelectedMetric() { return selectedMetric; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }
    public void setSelectedMetric(String metric) { selectedMetric.setValue(metric); }

    public void loadReports(String token) {
        loading.setValue(true);
        repository.getOwnMonthlyReport(token, new QuestBuilderRepository.RepositoryCallback<List<QuestMonthlyMetricResponse>>() {
            @Override public void onSuccess(List<QuestMonthlyMetricResponse> data, String text) {
                monthlyMetrics.setValue(data);
                loading.setValue(false);
            }
            @Override public void onError(String text) {
                loading.setValue(false);
                message.setValue(text);
            }
        });
        repository.getOwnRankingReport(token, new QuestBuilderRepository.RepositoryCallback<List<QuestRankingBoardResponse>>() {
            @Override public void onSuccess(List<QuestRankingBoardResponse> data, String text) { rankingBoard.setValue(data); }
            @Override public void onError(String text) { message.setValue(text); }
        });
        repository.getOwnEventReport(token, new QuestBuilderRepository.RepositoryCallback<List<QuestNodeMetricResponse>>() {
            @Override public void onSuccess(List<QuestNodeMetricResponse> data, String text) {
                nodeMetrics.setValue(data);
            }
            @Override public void onError(String text) { message.setValue(text); }
        });
    }
}
