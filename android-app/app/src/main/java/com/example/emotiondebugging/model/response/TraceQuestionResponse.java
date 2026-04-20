package com.example.emotiondebugging.model.response;

public class TraceQuestionResponse {
    private int question_id;
    private int error_type_id;
    private String error_name;
    private String question_text;
    private String option_1;
    private String option_2;
    private String option_3;
    private String option_4;

    public int getQuestion_id() {
        return question_id;
    }

    public int getError_type_id() {
        return error_type_id;
    }

    public String getError_name() {
        return error_name;
    }

    public String getQuestion_text() {
        return question_text;
    }

    public String getOption_1() {
        return option_1;
    }

    public String getOption_2() {
        return option_2;
    }

    public String getOption_3() {
        return option_3;
    }

    public String getOption_4() {
        return option_4;
    }
}