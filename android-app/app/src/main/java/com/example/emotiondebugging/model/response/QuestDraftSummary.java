package com.example.emotiondebugging.model.response;

public class QuestDraftSummary {
    public int quest_id;
    public String quest_title;
    public String quest_description;
    public int quest_level;
    public Integer error_type_id;
    public String error_name;
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

    @Override
    public String toString() {
        String title = quest_title == null || quest_title.trim().isEmpty()
                ? "Untitled quest" : quest_title;
        int version = latest_version_number == null ? 1 : latest_version_number;
        return title + "  •  v" + version;
    }
}
