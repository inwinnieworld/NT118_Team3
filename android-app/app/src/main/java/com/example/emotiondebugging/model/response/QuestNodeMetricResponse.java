package com.example.emotiondebugging.model.response;

public class QuestNodeMetricResponse {
    private int quest_id;
    private String quest_title;
    private String client_config_id;
    private String node_name;
    private String engine_subtype;
    private int started_runs;
    private int completed_runs;
    private float drop_off_rate;
    private float avg_duration_seconds;
    private int error_count;

    public int getQuestId() { return quest_id; }
    public String getQuestTitle() { return quest_title; }
    public String getClientConfigId() { return client_config_id; }
    public String getNodeName() { return node_name; }
    public String getEngineSubtype() { return engine_subtype; }
    public int getStartedRuns() { return started_runs; }
    public int getCompletedRuns() { return completed_runs; }
    public float getDropOffRate() { return drop_off_rate; }
    public float getAverageDurationSeconds() { return avg_duration_seconds; }
    public int getErrorCount() { return error_count; }
}
