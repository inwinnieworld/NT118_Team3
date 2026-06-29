package com.example.emotiondebugging.model.domain;

public class QuestCategory {
    private int error_type_id;
    private String error_name;

    public int getErrorTypeId() {
        return error_type_id;
    }

    public String getErrorName() {
        return error_name == null ? "" : error_name;
    }

    @Override
    public String toString() {
        return getErrorName();
    }
}
