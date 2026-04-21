package com.example.emotiondebugging.ui.staff;

import com.example.emotiondebugging.model.response.TraceQuestionResponse;

import java.util.List;

public class TraceQuestionGroupItem {
    private final int errorTypeId;
    private final String errorName;
    private final String errorCode;
    private final List<TraceQuestionResponse> questions;

    public TraceQuestionGroupItem(int errorTypeId, String errorName, String errorCode, List<TraceQuestionResponse> questions) {
        this.errorTypeId = errorTypeId;
        this.errorName = errorName;
        this.errorCode = errorCode;
        this.questions = questions;
    }

    public int getErrorTypeId() {
        return errorTypeId;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<TraceQuestionResponse> getQuestions() {
        return questions;
    }
}