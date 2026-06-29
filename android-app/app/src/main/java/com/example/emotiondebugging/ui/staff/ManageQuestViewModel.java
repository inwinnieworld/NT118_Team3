package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.QuestBuilderRepository;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;

import java.util.List;

public class ManageQuestViewModel extends ViewModel {
    private final QuestBuilderRepository repository = new QuestBuilderRepository();
    private final MutableLiveData<List<QuestDraftSummary>> quests = new MutableLiveData<>();
    private final MutableLiveData<QuestDraftDetail> preview = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<QuestDraftSummary>> getQuests() { return quests; }
    public LiveData<QuestDraftDetail> getPreview() { return preview; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Boolean> getLoading() { return loading; }
    public void clearPreview() { preview.setValue(null); }

    public void loadQuests(String token) {
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

    public void deleteDraft(String token, int questId) {
        loading.setValue(true);
        repository.deleteDraft(token, questId, new QuestBuilderRepository.RepositoryCallback<Object>() {
            @Override public void onSuccess(Object data, String text) {
                message.setValue("Draft deleted");
                loadQuests(token);
            }
            @Override public void onError(String text) {
                loading.setValue(false);
                message.setValue(text);
            }
        });
    }
}
