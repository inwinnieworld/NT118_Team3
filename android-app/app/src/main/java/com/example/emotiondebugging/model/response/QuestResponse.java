package com.example.emotiondebugging.model.response;

public class QuestResponse {
    private int quest_id;
    private int error_type_id;
    private String error_name;
    private String quest_title;
    private String quest_description;

    private int base_priority;
    private String tag;
    private int estimated_duration;
    private int level_severity;

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

    public int getBase_priority() {
        return base_priority;
    }

    public String getTag() {
        return tag;
    }

    public int getEstimated_duration() {
        return estimated_duration;
    }

    public int getLevel_severity() {
        return level_severity;
    }
}