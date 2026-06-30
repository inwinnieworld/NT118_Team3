package com.example.emotiondebugging.model.response;

import com.example.emotiondebugging.model.request.QuestDraftRequest;

import java.util.List;
import java.util.Map;

public class QuestDraftDetail {
    public int version_id;
    public int quest_id;
    public int version_number;
    public String version_status;
    public String quest_title;
    public String quest_description;
    public int quest_level;
    public String problem_id;
    public String problem_title;
    public String problem_path;
    public List<String> ai_tags;
    public int intensity_min;
    public int intensity_max;
    public String therapeutic_goal;
    public Integer estimated_duration_seconds;
    public Map<String, Object> canvas_config;
    public QuestDraftRequest.QuestFlow flow;
}
