package com.example.emotiondebugging.model.response;

public class QuestResponse {
    private int quest_id;
    private int error_type_id;
    private String error_name;
    private String quest_title;
    private String quest_description;

    public int getQuest_id() {
        return quest_id;
    }

    public int getError_type_id() {
        return error_type_id;
    }

    public String getError_name() {
        return error_name;
    }

    public String getQuest_title() {
        return quest_title;
    }

    public String getQuest_description() {
        return quest_description;
    }
}