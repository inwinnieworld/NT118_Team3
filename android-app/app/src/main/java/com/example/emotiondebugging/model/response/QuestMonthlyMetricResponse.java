package com.example.emotiondebugging.model.response;

public class QuestMonthlyMetricResponse {
    private String chart_month;
    private float avg_severity;
    private float severity_rate;
    private int total_errors;
    private float acceptance_rate;

    public String getChart_month() {
        return chart_month;
    }

    public float getAvg_severity() {
        return avg_severity;
    }

    public float getSeverity_rate() {
        return severity_rate;
    }

    public int getTotal_errors() {
        return total_errors;
    }

    public float getAcceptance_rate() {
        return acceptance_rate;
    }
}