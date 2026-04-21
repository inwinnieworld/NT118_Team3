package com.example.emotiondebugging.model.response;

public class QuestSummaryReportResponse {
    private int total_error_logs;
    private int total_quests;
    private int total_assignments;
    private int completed_assignments;
    private double avg_feedback;

    public int getTotal_error_logs() {
        return total_error_logs;
    }

    public int getTotal_quests() {
        return total_quests;
    }

    public int getTotal_assignments() {
        return total_assignments;
    }

    public int getCompleted_assignments() {
        return completed_assignments;
    }

    public double getAvg_feedback() {
        return avg_feedback;
    }
}