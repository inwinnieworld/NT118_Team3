package com.example.emotiondebugging.model.request;

public class ValidateResetTokenRequest {
    private String token;

    public ValidateResetTokenRequest() {
    }

    public ValidateResetTokenRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}