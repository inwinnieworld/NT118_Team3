package com.example.emotiondebugging.model.response;

public class QuestRankingReportResponse {
    private int quest_id;
    private String quest_title;
    private int total_assigned;
    private int total_completed;

    public int getQuest_id() {
        return quest_id;
    }

    public String getQuest_title() {
        return quest_title;
    }

    public int getTotal_assigned() {
        return total_assigned;
    }

    public int getTotal_completed() {
        return total_completed;
    }
}