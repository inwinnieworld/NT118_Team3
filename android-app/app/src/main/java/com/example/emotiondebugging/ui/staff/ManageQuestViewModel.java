package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.StaffRepository;
import com.example.emotiondebugging.model.request.CreateQuestRequest;
import com.example.emotiondebugging.model.response.QuestResponse;

import java.util.List;

public class ManageQuestViewModel extends ViewModel {

    private final StaffRepository repository = new StaffRepository();

    private final MutableLiveData<List<QuestResponse>> quests = new MutableLiveData<>();
    private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<QuestResponse>> getQuests() {
        return quests;
    }

    public LiveData<Boolean> getCreateSuccess() {
        return createSuccess;
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

    public void loadQuests() {
        repository.getAllQuests(quests, message, loading);
    }

    public void createQuest(String errorTypeIdText, String title, String description) {
        createSuccess.setValue(false);

        if (title == null || title.trim().isEmpty()) {
            message.setValue("Tên quest không được để trống");
            return;
        }

        int errorTypeId;
        try {
            errorTypeId = Integer.parseInt(errorTypeIdText.trim());
        } catch (Exception e) {
            message.setValue("Error Type ID không hợp lệ");
            return;
        }

        repository.createQuest(
                new CreateQuestRequest(
                        errorTypeId,
                        title.trim(),
                        description == null ? "" : description.trim()
                ),
                createSuccess,
                message,
                loading
        );
    }

    public void deleteQuest(int questId) {
        deleteSuccess.setValue(false);
        repository.deleteQuest(questId, deleteSuccess, message, loading);
    }
}