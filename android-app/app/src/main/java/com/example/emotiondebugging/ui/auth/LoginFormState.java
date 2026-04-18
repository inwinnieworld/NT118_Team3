package com.example.emotiondebugging.ui.auth;

public class LoginFormState {

    private final String accountError;
    private final String passwordError;
    private final boolean dataValid;

    public LoginFormState(String accountError, String passwordError, boolean dataValid) {
        this.accountError = accountError;
        this.passwordError = passwordError;
        this.dataValid = dataValid;
    }

    public String getAccountError() {
        return accountError;
    }

    public String getPasswordError() {
        return passwordError;
    }

    public boolean isDataValid() {
        return dataValid;
    }
}