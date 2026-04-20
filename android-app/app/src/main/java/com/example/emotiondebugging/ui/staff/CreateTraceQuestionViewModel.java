package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.StaffRepository;
import com.example.emotiondebugging.model.request.CreateTraceQuestionRequest;

public class CreateTraceQuestionViewModel extends ViewModel {

    private final StaffRepository repository = new StaffRepository();

    private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<Boolean> getCreateSuccess() {
        return createSuccess;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void createTraceQuestion(int errorTypeId,
                                    String questionText,
                                    String option1,
                                    String option2,
                                    String option3,
                                    String option4) {
        if (questionText == null || questionText.trim().isEmpty()) {
            message.setValue("Nội dung câu hỏi không được để trống");
            createSuccess.setValue(false);
            return;
        }

        repository.createTraceQuestion(
                new CreateTraceQuestionRequest(
                        errorTypeId,
                        questionText.trim(),
                        option1 == null ? "" : option1.trim(),
                        option2 == null ? "" : option2.trim(),
                        option3 == null ? "" : option3.trim(),
                        option4 == null ? "" : option4.trim()
                ),
                createSuccess,
                message,
                loading
        );
    }
}