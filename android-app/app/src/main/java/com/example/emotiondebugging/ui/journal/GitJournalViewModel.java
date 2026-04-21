package com.example.emotiondebugging.ui.journal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.GitJournalRepository;
import com.example.emotiondebugging.model.Emotion;
import com.example.emotiondebugging.model.response.CreateCommitResponse;

import java.util.List;

public class GitJournalViewModel extends ViewModel {

    private final GitJournalRepository repository;

    private final MutableLiveData<Boolean> _commitSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getCommitSuccess() {
        return _commitSuccess;
    }

    private final MutableLiveData<CreateCommitResponse.SeverityAlert> _severityAlert = new MutableLiveData<>();
    public LiveData<CreateCommitResponse.SeverityAlert> getSeverityAlert() {
        return _severityAlert;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> getLoading() {
        return _loading;
    }

    private final MutableLiveData<List<Emotion>> _emotions = new MutableLiveData<>();
    public LiveData<List<Emotion>> getEmotions() {
        return _emotions;
    }

    public GitJournalViewModel() {
        this.repository = new GitJournalRepository();
    }

    /**
     * Load emotions from backend
     */
    public void loadEmotions(String token) {
        _loading.setValue(true);
        repository.loadEmotions(token, _emotions, _errorMessage);
        _loading.setValue(false);
    }

    /**
     * Push commit to backend
     * @param token JWT token
     * @param emotion Selected emotion name
     * @param intensity Intensity value (0-100)
     * @param message Commit message
     * @param branch Branch name (main or quest/xxx)
     */
    public void pushCommit(String token, String emotion, int intensity, String message, String branch) {
        _loading.setValue(true);

        MutableLiveData<CreateCommitResponse> commitResponseLiveData = new MutableLiveData<>();
        
        // Observe commit response
        commitResponseLiveData.observeForever(response -> {
            if (response != null && response.isSuccess()) {
                _commitSuccess.postValue(true);
                
                // Check for severity alert
                if (response.getData() != null && response.getData().getAlert() != null) {
                    CreateCommitResponse.SeverityAlert alert = response.getData().getAlert();
                    if (alert.isShouldAlert()) {
                        _severityAlert.postValue(alert);
                    }
                }
            }
            _loading.postValue(false);
        });

        // Call repository
        repository.createCommit(token, emotion, intensity, message, commitResponseLiveData, _errorMessage);
    }

    /**
     * Clear session state
     */
    public void clearSession() {
        _commitSuccess.setValue(null);
        _errorMessage.setValue(null);
        _severityAlert.setValue(null);
    }
}
