package com.example.emotiondebugging.ui.staff;

import java.util.List;

public class TraceQuestionItem {
    private String errorName;
    private String errorCode;
    private List<String> questions;

    public TraceQuestionItem(String errorName, String errorCode, List<String> questions) {
        this.errorName = errorName;
        this.errorCode = errorCode;
        this.questions = questions;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<String> getQuestions() {
        return questions;
    }
}