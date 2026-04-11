package com.example.emotiondebugging.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.AuthRepository;
import com.example.emotiondebugging.model.request.LoginRequest;
import com.example.emotiondebugging.model.response.LoginResponse;

public class LoginViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public void login(String account, String password) {
        if (account == null || account.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            message.setValue("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        loading.setValue(true);

        repository.login(
                new LoginRequest(account.trim(), password.trim()),
                new AuthRepository.RepositoryCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse data, String msg) {
                        loading.postValue(false);
                        loginResponse.postValue(data);
                        message.postValue(msg);
                    }

                    @Override
                    public void onError(String msg) {
                        loading.postValue(false);
                        message.postValue(msg);
                    }
                }
        );
    }
}