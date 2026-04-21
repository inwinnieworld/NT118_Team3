package com.example.emotiondebugging.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Model for Git Graph Daily Merge Node
 * Represents a merge point in the graph
 */
public class GitGraphMerge {
    private int merge_id;
    private int student_id;
    private String merge_date;
    private int dominant_emotion_id;
    private String emotion_name;
    private String emotion_category;
    private String color_hex;
    
    @SerializedName("emotion_stats")
    private Map<String, EmotionStat> emotionStats;
    
    private String user_retrospective;
    private boolean is_auto_merged;
    private String created_at;

    // Inner class for emotion statistics
    public static class EmotionStat {
        private int emotion_id;
        private String emotion_name;
        private String emotion_category;
        private String color_hex;
        private double frequency;
        private double avg_intensity;
        private double impact_score;

        // Getters and Setters
        public int getEmotion_id() {
            return emotion_id;
        }

        public void setEmotion_id(int emotion_id) {
            this.emotion_id = emotion_id;
        }

        public String getEmotion_name() {
            return emotion_name;
        }

        public void setEmotion_name(String emotion_name) {
            this.emotion_name = emotion_name;
        }

        public String getEmotion_category() {
            return emotion_category;
        }

        public void setEmotion_category(String emotion_category) {
            this.emotion_category = emotion_category;
        }

        public String getColor_hex() {
            return color_hex;
        }

        public void setColor_hex(String color_hex) {
            this.color_hex = color_hex;
        }

        public double getFrequency() {
            return frequency;
        }

        public void setFrequency(double frequency) {
            this.frequency = frequency;
        }

        public double getAvg_intensity() {
            return avg_intensity;
        }

        public void setAvg_intensity(double avg_intensity) {
            this.avg_intensity = avg_intensity;
        }

        public double getImpact_score() {
            return impact_score;
        }

        public void setImpact_score(double impact_score) {
            this.impact_score = impact_score;
        }
    }

    // Constructors
    public GitGraphMerge() {}

    // Getters and Setters
    public int getMerge_id() {
        return merge_id;
    }

    public void setMerge_id(int merge_id) {
        this.merge_id = merge_id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public String getMerge_date() {
        return merge_date;
    }

    public void setMerge_date(String merge_date) {
        this.merge_date = merge_date;
    }

    public int getDominant_emotion_id() {
        return dominant_emotion_id;
    }

    public void setDominant_emotion_id(int dominant_emotion_id) {
        this.dominant_emotion_id = dominant_emotion_id;
    }

    public String getEmotion_name() {
        return emotion_name;
    }

    public void setEmotion_name(String emotion_name) {
        this.emotion_name = emotion_name;
    }

    public String getEmotion_category() {
        return emotion_category;
    }

    public void setEmotion_category(String emotion_category) {
        this.emotion_category = emotion_category;
    }

    public String getColor_hex() {
        return color_hex;
    }

    public void setColor_hex(String color_hex) {
        this.color_hex = color_hex;
    }

    public Map<String, EmotionStat> getEmotionStats() {
        return emotionStats;
    }

    public void setEmotionStats(Map<String, EmotionStat> emotionStats) {
        this.emotionStats = emotionStats;
    }

    public String getUser_retrospective() {
        return user_retrospective;
    }

    public void setUser_retrospective(String user_retrospective) {
        this.user_retrospective = user_retrospective;
    }

    public boolean isIs_auto_merged() {
        return is_auto_merged;
    }

    public void setIs_auto_merged(boolean is_auto_merged) {
        this.is_auto_merged = is_auto_merged;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
