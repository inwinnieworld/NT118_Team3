package com.example.emotiondebugging.ui.community;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.CommunityRepository;
import com.example.emotiondebugging.model.request.CreatePostRequest;
import com.example.emotiondebugging.model.response.CommunityPostResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommunityViewModel extends ViewModel {

    private final CommunityRepository repository = new CommunityRepository();

    private final MutableLiveData<CommunityPostResponse> _posts = new MutableLiveData<>();
    public LiveData<CommunityPostResponse> getPosts() {
        return _posts;
    }

    private final MutableLiveData<List<Map<String, Object>>> _errorTypes = new MutableLiveData<>();
    public LiveData<List<Map<String, Object>>> getErrorTypes() {
        return _errorTypes;
    }

    private final MutableLiveData<Map<String, Object>> _createPostResult = new MutableLiveData<>();
    public LiveData<Map<String, Object>> getCreatePostResult() {
        return _createPostResult;
    }

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> getLoading() {
        return _loading;
    }

    private final MutableLiveData<String> _message = new MutableLiveData<>();
    public LiveData<String> getMessage() {
        return _message;
    }

    public void loadPosts(String token, String filter, int page, String search) {
        loadPosts(token, filter, page, search, null);
    }

    public void loadPosts(
            String token,
            String filter,
            int page,
            String search,
            Integer errorTypeId
    ) {
        _loading.setValue(true);

        repository.getPosts(
                token,
                filter,
                page,
                search,
                errorTypeId,
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
                }
        );
    }

    /**
     * Lọc bài viết theo hashtag ở phía Android.
     *
     * Hiện tại chưa sửa DB/backend nên hashtag được lấy theo 2 nguồn:
     * 1. post.hashtags nếu backend có trả field hashtags
     * 2. post.errorName nếu backend vẫn đang dùng error type cũ
     */
    public void loadPostsByHashtag(
            String token,
            String filter,
            int page,
            String hashtag
    ) {
        _loading.setValue(true);

        repository.getPosts(
                token,
                filter,
                page,
                "",
                null,
                new CommunityRepository.RepositoryCallback<CommunityPostResponse>() {
                    @Override
                    public void onSuccess(CommunityPostResponse data, String msg) {
                        _loading.postValue(false);

                        if (data == null || data.posts == null) {
                            _posts.postValue(data);
                            return;
                        }

                        String targetHashtag = normalizeHashtag(hashtag);
                        List<CommunityPostResponse.PostItem> filteredPosts = new ArrayList<>();

                        for (CommunityPostResponse.PostItem post : data.posts) {
                            if (hasPostHashtag(post, targetHashtag)) {
                                filteredPosts.add(post);
                            }
                        }

                        data.posts = filteredPosts;
                        data.total = filteredPosts.size();
                        data.page = page;
                        data.totalPages = 1;

                        _posts.postValue(data);
                    }

                    @Override
                    public void onError(String msg) {
                        _loading.postValue(false);
                        _message.postValue(msg);
                    }
                }
        );
    }

    private boolean hasPostHashtag(
            CommunityPostResponse.PostItem post,
            String targetHashtag
    ) {
        if (post == null || targetHashtag == null || targetHashtag.isEmpty()) {
            return false;
        }

        if (post.hashtags != null && !post.hashtags.isEmpty()) {
            for (String tag : post.hashtags) {
                String normalizedTag = normalizeHashtag(tag);

                if (targetHashtag.equals(normalizedTag)) {
                    return true;
                }
            }
        }

        // Fallback tạm thời: nếu backend chưa trả hashtags thì dùng errorName làm hashtag
        if (post.errorName != null && !post.errorName.trim().isEmpty()) {
            String normalizedErrorName = normalizeHashtag(post.errorName);

            if (targetHashtag.equals(normalizedErrorName)) {
                return true;
            }
        }

        return false;
    }

    private String normalizeHashtag(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replace("#", "")
                .replace(" ", "-")
                .toLowerCase(Locale.ROOT);
    }

    public void loadErrorTypes(String token) {
        repository.getErrorTypes(
                token,
                new CommunityRepository.RepositoryCallback<List<Map<String, Object>>>() {
                    @Override
                    public void onSuccess(List<Map<String, Object>> data, String msg) {
                        _errorTypes.postValue(data);
                    }

                    @Override
                    public void onError(String msg) {
                        _message.postValue(msg);
                    }
                }
        );
    }

    public void createPost(
            String token,
            String title,
            String content,
            int errorTypeId,
            boolean isAnonymous
    ) {
        _loading.setValue(true);

        repository.createPost(
                token,
                new CreatePostRequest(title, content, errorTypeId, isAnonymous),
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
                }
        );
    }

    public void votePost(String token, int postId, String voteType) {
        repository.votePost(
                token,
                postId,
                voteType,
                new CommunityRepository.RepositoryCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg) {
                        _message.postValue(msg);
                    }

                    @Override
                    public void onError(String msg) {
                        _message.postValue(msg);
                    }
                }
        );
    }
}