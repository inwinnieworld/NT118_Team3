package com.example.emotiondebugging.ui.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.QuestBuilderRepository;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;

import java.util.List;
import java.util.Map;

public class AdminQuestReviewViewModel extends ViewModel {
    private final QuestBuilderRepository repository = new QuestBuilderRepository();
    private final MutableLiveData<List<QuestDraftSummary>> quests = new MutableLiveData<>();
    private final MutableLiveData<QuestDraftDetail> preview = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<List<QuestDraftSummary>> getQuests() { return quests; }
    public LiveData<QuestDraftDetail> getPreview() { return preview; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }

    public void loadPending(String token) {
        loading.setValue(true);
        repository.getQuestsByStatus(token, null, new QuestBuilderRepository.RepositoryCallback<List<QuestDraftSummary>>() {
            @Override public void onSuccess(List<QuestDraftSummary> data, String text) {
                loading.setValue(false);
                quests.setValue(data);
            }
            @Override public void onError(String text) {
                loading.setValue(false);
                message.setValue(text);
            }
        });
    }

    public void updateVisibility(String token, int questId, boolean active) {
        loading.setValue(true);
        repository.updateQuestVisibility(token, questId, active, new QuestBuilderRepository.RepositoryCallback<Map<String, Object>>() {
            @Override public void onSuccess(Map<String, Object> data, String text) {
                message.setValue(active ? "Quest restored" : "Quest hidden from students");
                loadPending(token);
            }
            @Override public void onError(String text) {
                loading.setValue(false);
                message.setValue(text);
            }
        });
    }

    public void loadPreview(String token, int versionId) {
        loading.setValue(true);
        repository.getDraftVersion(token, versionId, new QuestBuilderRepository.RepositoryCallback<QuestDraftDetail>() {
            @Override public void onSuccess(QuestDraftDetail data, String text) {
                loading.setValue(false);
                preview.setValue(data);
            }
            @Override public void onError(String text) {
                loading.setValue(false);
                message.setValue(text);
            }
        });
    }

    public void review(String token, int questId, String action, String note) {
        loading.setValue(true);
        repository.reviewQuest(token, questId, action, note, new QuestBuilderRepository.RepositoryCallback<Map<String, Object>>() {
            @Override public void onSuccess(Map<String, Object> data, String text) {
                message.setValue("approved".equals(action) ? "Quest approved" : "Quest rejected");
                loadPending(token);
            }
            @Override public void onError(String text) {
                loading.setValue(false);
                message.setValue(text);
            }
        });
    }
}
