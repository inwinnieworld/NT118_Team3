package com.example.emotiondebugging.model.response;

public class QuestMonthlyMetricResponse {
    private String chart_month;
    private int total_runs;
    private float completion_rate;
    private float abandonment_rate;
    private float avg_duration_minutes;

    public String getChart_month() {
        return chart_month;
    }

    public int getTotalRuns() { return total_runs; }
    public float getCompletionRate() { return completion_rate; }
    public float getAbandonmentRate() { return abandonment_rate; }
    public float getAverageDurationMinutes() { return avg_duration_minutes; }
}
