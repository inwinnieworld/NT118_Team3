package com.example.emotiondebugging.model.response;

public class QuestTrendItemResponse {
    private String chart_date;
    private float total_assigned;
    private float total_completed;

    public String getChart_date() {
        return chart_date;
    }

    public float getTotal_assigned() {
        return total_assigned;
    }

    public float getTotal_completed() {
        return total_completed;
    }
}