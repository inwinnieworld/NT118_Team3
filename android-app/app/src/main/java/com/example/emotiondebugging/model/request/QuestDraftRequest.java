package com.example.emotiondebugging.model.request;

import java.util.List;
import java.util.Map;

public class QuestDraftRequest {
    public Integer quest_id;
    public String quest_title;
    public String quest_description;
    public int quest_level;
    public Integer error_type_id;
    public List<String> ai_tags;
    public int intensity_min;
    public int intensity_max;
    public String therapeutic_goal;
    public int estimated_duration_seconds;
    public Map<String, Object> canvas_config;
    public QuestFlow flow;

    public static class QuestFlow {
        public List<QuestFlowNode> nodes;
        public List<QuestFlowEdge> edges;
    }

    public static class QuestFlowNode {
        public String client_node_id;
        public String parent_client_node_id;
        public String terminal_client_node_id;
        public String engine_type;
        public String engine_subtype;
        public String display_name;
        public double position_x;
        public double position_y;
        public Double width;
        public Double height;
        public int z_index;
        public Map<String, Object> config;
    }

    public static class QuestFlowEdge {
        public String client_edge_id;
        public String source_client_node_id;
        public String target_client_node_id;
        public String flow_type;
        public String completion_condition;
        public int sort_order;
        public Map<String, Object> config;
    }
}
