package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.StaffRepository;
import com.example.emotiondebugging.model.request.UpdateTraceQuestionRequest;
import com.example.emotiondebugging.model.response.TraceQuestionResponse;

public class TraceQuestionDetailViewModel extends ViewModel {

    private final StaffRepository repository = new StaffRepository();

    private final MutableLiveData<TraceQuestionResponse> detail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<TraceQuestionResponse> getDetail() {
        return detail;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadDetail(int questionId) {
        repository.getTraceQuestionDetail(questionId, detail, message, loading);
    }

    public void updateQuestion(int questionId,
                               int errorTypeId,
                               String questionText,
                               String option1,
                               String option2,
                               String option3,
                               String option4) {
        repository.updateTraceQuestion(
                questionId,
                new UpdateTraceQuestionRequest(
                        errorTypeId,
                        questionText == null ? "" : questionText.trim(),
                        option1 == null ? "" : option1.trim(),
                        option2 == null ? "" : option2.trim(),
                        option3 == null ? "" : option3.trim(),
                        option4 == null ? "" : option4.trim()
                ),
                updateSuccess,
                message,
                loading
        );
    }

    public void deleteQuestion(int questionId) {
        repository.deleteTraceQuestion(questionId, deleteSuccess, message, loading);
    }
}