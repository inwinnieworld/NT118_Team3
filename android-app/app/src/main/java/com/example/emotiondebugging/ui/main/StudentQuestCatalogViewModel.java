package com.example.emotiondebugging.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.QuestBuilderRepository;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;
import com.example.emotiondebugging.model.domain.QuestProblem;

import java.util.List;
import java.util.Map;

public class StudentQuestCatalogViewModel extends ViewModel {
    public static class RunLaunch {
        public final QuestDraftDetail detail;
        public final int runId;
        RunLaunch(QuestDraftDetail detail, int runId) {
            this.detail = detail;
            this.runId = runId;
        }
    }

    private final QuestBuilderRepository repository = new QuestBuilderRepository();
    private final MutableLiveData<List<QuestDraftSummary>> quests = new MutableLiveData<>();
    private final MutableLiveData<List<QuestProblem>> problems = new MutableLiveData<>();
    private final MutableLiveData<RunLaunch> launch = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<List<QuestDraftSummary>> getQuests() { return quests; }
    public LiveData<List<QuestProblem>> getProblems() { return problems; }
    public LiveData<RunLaunch> getLaunch() { return launch; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }

    public void loadProblems(String token) {
        repository.getProblems(token, new QuestBuilderRepository.RepositoryCallback<List<QuestProblem>>() {
            @Override public void onSuccess(List<QuestProblem> data, String text) {
                problems.setValue(data);
            }
            @Override public void onError(String text) { message.setValue(text); }
        });
    }

    public void loadCatalog(String token, String problemId) {
        loading.setValue(true);
        repository.getApprovedCatalog(token, problemId, new QuestBuilderRepository.RepositoryCallback<List<QuestDraftSummary>>() {
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

    public void startQuest(String token, int questId) {
        loading.setValue(true);
        repository.getApprovedFlow(token, questId, new QuestBuilderRepository.RepositoryCallback<QuestDraftDetail>() {
            @Override public void onSuccess(QuestDraftDetail detail, String text) {
                repository.startQuestRun(token, questId, new QuestBuilderRepository.RepositoryCallback<Map<String, Object>>() {
                    @Override public void onSuccess(Map<String, Object> data, String runMessage) {
                        loading.setValue(false);
                        Object value = data == null ? null : data.get("run_id");
                        int runId = value instanceof Number ? ((Number) value).intValue() : 0;
                        if (runId <= 0) message.setValue("Server did not create a quest run");
                        else launch.setValue(new RunLaunch(detail, runId));
                    }
                    @Override public void onError(String runError) {
                        loading.setValue(false);
                        message.setValue(runError);
                    }
                });
            }
            @Override public void onError(String text) {
                loading.setValue(false);
                message.setValue(text);
            }
        });
    }
}
