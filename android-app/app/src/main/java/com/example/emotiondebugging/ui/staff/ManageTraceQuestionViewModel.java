package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.StaffRepository;
import com.example.emotiondebugging.model.response.TraceQuestionResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManageTraceQuestionViewModel extends ViewModel {

    private final StaffRepository repository = new StaffRepository();

    private final MutableLiveData<List<TraceQuestionResponse>> rawTraceQuestions = new MutableLiveData<>();
    private final MutableLiveData<List<TraceQuestionGroupItem>> groupedTraceQuestions = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> selectedErrorTypeId = new MutableLiveData<>(-1);

    public LiveData<List<TraceQuestionGroupItem>> getGroupedTraceQuestions() {
        return groupedTraceQuestions;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Integer> getSelectedErrorTypeId() {
        return selectedErrorTypeId;
    }

    public void setSelectedErrorTypeId(int errorTypeId) {
        selectedErrorTypeId.setValue(errorTypeId);
        regroupData();
    }

    public void loadTraceQuestions() {
        repository.getAllTraceQuestions(rawTraceQuestions, message, loading);
    }

    public void observeSource() {
        rawTraceQuestions.observeForever(list -> regroupData());
    }

    private void regroupData() {
        List<TraceQuestionResponse> source = rawTraceQuestions.getValue();
        Integer selectedId = selectedErrorTypeId.getValue();

        if (source == null) {
            groupedTraceQuestions.setValue(new ArrayList<>());
            return;
        }

        Map<Integer, List<TraceQuestionResponse>> groupedMap = new LinkedHashMap<>();
        Map<Integer, String> errorNameMap = new LinkedHashMap<>();

        for (TraceQuestionResponse item : source) {
            if (selectedId != null && selectedId != -1 && item.getError_type_id() != selectedId) {
                continue;
            }

            int errorTypeId = item.getError_type_id();
            if (!groupedMap.containsKey(errorTypeId)) {
                groupedMap.put(errorTypeId, new ArrayList<>());
                errorNameMap.put(errorTypeId, item.getError_name());
            }
            groupedMap.get(errorTypeId).add(item);
        }

        List<TraceQuestionGroupItem> result = new ArrayList<>();
        for (Map.Entry<Integer, List<TraceQuestionResponse>> entry : groupedMap.entrySet()) {
            int errorTypeId = entry.getKey();
            String errorName = errorNameMap.get(errorTypeId);

            result.add(new TraceQuestionGroupItem(
                    errorTypeId,
                    errorName,
                    "bug_report",
                    entry.getValue()
            ));
        }

        groupedTraceQuestions.setValue(result);
    }
}