package com.example.emotiondebugging.model.response;

import com.example.emotiondebugging.model.Emotion;

import java.util.List;

public class EmotionsResponse {
    private boolean success;
    private List<Emotion> data;
    private String message;

    // Constructors
    public EmotionsResponse() {}

    public EmotionsResponse(boolean success, List<Emotion> data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Emotion> getData() {
        return data;
    }

    public void setData(List<Emotion> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
