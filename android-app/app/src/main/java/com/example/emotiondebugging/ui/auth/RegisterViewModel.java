package com.example.emotiondebugging.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.AuthRepository;
import com.example.emotiondebugging.model.request.RegisterRequest;

public class RegisterViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> success = new MutableLiveData<>(false);

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getSuccess() {
        return success;
    }

    public void register(String fullName, String email, String password, String studentCode) {
        loading.setValue(true);

        repository.register(
                new RegisterRequest(
                        fullName,
                        email,
                        password,
                        "",
                        studentCode,
                        "",
                        "",
                        null
                ),
                new AuthRepository.RepositoryCallback<Object>() {
                    @Override
                    public void onSuccess(Object data, String msg) {
                        loading.setValue(false);
                        success.setValue(true);
                        message.setValue(msg);
                    }

                    @Override
                    public void onError(String msg) {
                        loading.setValue(false);
                        success.setValue(false);
                        message.setValue(msg);
                    }
                }
        );
    }
}