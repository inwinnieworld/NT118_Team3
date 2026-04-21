package com.example.emotiondebugging.model;

/**
 * Model for Git Graph Commit Node
 * Represents a single commit in the graph visualization
 */
public class GitGraphCommit {
    private int commit_id;
    private int student_id;
    private int emotion_id;
    private String emotion_name;
    private String emotion_category;
    private String color_hex;
    private String icon_url;
    private String branch_type; // "main" or "quest"
    private Integer user_quest_id; // null for main, quest_id for quest
    private int intensity_level;
    private String message;
    private String created_at;

    // Constructors
    public GitGraphCommit() {}

    // Getters and Setters
    public int getCommit_id() {
        return commit_id;
    }

    public void setCommit_id(int commit_id) {
        this.commit_id = commit_id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

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

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getBranch_type() {
        return branch_type;
    }

    public void setBranch_type(String branch_type) {
        this.branch_type = branch_type;
    }

    public Integer getUser_quest_id() {
        return user_quest_id;
    }

    public void setUser_quest_id(Integer user_quest_id) {
        this.user_quest_id = user_quest_id;
    }

    public int getIntensity_level() {
        return intensity_level;
    }

    public void setIntensity_level(int intensity_level) {
        this.intensity_level = intensity_level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    // Helper methods
    public boolean isMainBranch() {
        return "main".equals(branch_type);
    }

    public boolean isQuestBranch() {
        return "quest".equals(branch_type);
    }
}
