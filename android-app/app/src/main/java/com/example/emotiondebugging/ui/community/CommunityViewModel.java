package com.example.emotiondebugging.ui.community;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.CommunityRepository;
import com.example.emotiondebugging.model.request.CreatePostRequest;
import com.example.emotiondebugging.model.response.CommunityPostResponse;

import java.util.List;
import java.util.Map;

public class CommunityViewModel extends ViewModel {

    private final CommunityRepository repository = new CommunityRepository();

    private final MutableLiveData<CommunityPostResponse> _posts = new MutableLiveData<>();
    public LiveData<CommunityPostResponse> getPosts() { return _posts; }

    private final MutableLiveData<List<Map<String, Object>>> _errorTypes = new MutableLiveData<>();
    public LiveData<List<Map<String, Object>>> getErrorTypes() { return _errorTypes; }

    private final MutableLiveData<Map<String, Object>> _createPostResult = new MutableLiveData<>();
    public LiveData<Map<String, Object>> getCreatePostResult() { return _createPostResult; }

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> getLoading() { return _loading; }

    private final MutableLiveData<String> _message = new MutableLiveData<>();
    public LiveData<String> getMessage() { return _message; }

    public void loadPosts(String token, String filter, int page, String search) {
        loadPosts(token, filter, page, search, null);
    }

    public void loadPosts(String token, String filter, int page, String search, Integer errorTypeId) {
        _loading.setValue(true);
        repository.getPosts(token, filter, page, search, errorTypeId,
                new CommunityRepository.RepositoryCallback<CommunityPostResponse>() {
                    @Override
                    public void onSuccess(CommunityPostResponse data, String msg) {
                        _loading.postValue(false);
                        _posts.postValue(data);
                    }
                    @Override
                    public void onError(String msg) {
                        _loading.postValue(false);
                        _message.postValue(msg);
                    }
                });
    }

    public void loadErrorTypes(String token) {
        repository.getErrorTypes(token,
                new CommunityRepository.RepositoryCallback<List<Map<String, Object>>>() {
                    @Override
                    public void onSuccess(List<Map<String, Object>> data, String msg) {
                        _errorTypes.postValue(data);
                    }
                    @Override
                    public void onError(String msg) {
                        _message.postValue(msg);
                    }
                });
    }

    public void createPost(String token, String title, String content,
                           int errorTypeId, boolean isAnonymous) {
        _loading.setValue(true);
        repository.createPost(token, new CreatePostRequest(title, content, errorTypeId, isAnonymous),
                new CommunityRepository.RepositoryCallback<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> data, String msg) {
                        _loading.postValue(false);
                        _createPostResult.postValue(data);
                        _message.postValue(msg);
                    }
                    @Override
                    public void onError(String msg) {
                        _loading.postValue(false);
                        _message.postValue(msg);
                    }
                });
    }

    public void votePost(String token, int postId, String voteType) {
        repository.votePost(token, postId, voteType,
                new CommunityRepository.RepositoryCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg) {
                        _message.postValue(msg);
                    }
                    @Override
                    public void onError(String msg) {
                        _message.postValue(msg);
                    }
                });
    }
}
