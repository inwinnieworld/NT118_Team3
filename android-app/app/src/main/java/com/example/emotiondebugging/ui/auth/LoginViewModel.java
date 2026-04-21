package com.example.emotiondebugging.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.AuthRepository;
import com.example.emotiondebugging.model.request.LoginRequest;
import com.example.emotiondebugging.model.response.LoginResponse;
import com.example.emotiondebugging.utils.ValidationHelper;

public class LoginViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    // Validate input fields before attempting login
    public boolean validateLoginInput(String account, String password) {
        String accountError = ValidationHelper.validateLoginAccount(account);
        String passwordError = ValidationHelper.validateLoginPassword(password);

        boolean isValid = accountError == null && passwordError == null;
        loginFormState.setValue(new LoginFormState(accountError, passwordError, isValid));

        return isValid;
    }

    // Login method with validation
    public void login(String account, String password) {
        if (!validateLoginInput(account, password)) {
            message.setValue("Vui lòng kiểm tra lại thông tin đăng nhập");
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