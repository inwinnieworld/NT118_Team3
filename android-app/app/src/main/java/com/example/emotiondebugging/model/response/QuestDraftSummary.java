package com.example.emotiondebugging.model.response;

public class QuestDraftSummary {
    public int quest_id;
    public String quest_title;
    public String quest_description;
    public int quest_level;
    public String problem_id;
    public String problem_title;
    public String problem_path;
    public String approval_status;
    public boolean is_active;
    public java.util.List<String> ai_tags;
    public int intensity_min;
    public int intensity_max;
    public String therapeutic_goal;
    public Integer estimated_duration_seconds;
    public String review_note;
    public Integer latest_version_id;
    public Integer latest_version_number;
    public boolean is_completed;

    @Override
    public String toString() {
        String title = quest_title == null || quest_title.trim().isEmpty()
                ? "Untitled quest" : quest_title;
        int version = latest_version_number == null ? 1 : latest_version_number;
        return title + "  •  v" + version;
    }
}
