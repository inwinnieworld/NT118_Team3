package com.example.emotiondebugging.model.request;

public class CreateCommitRequest {
    private int emotion_id;
    private String branch_type; // "main" or "quest"
    private Integer user_quest_id; // null for main branch
    private int intensity_level;
    private String message;

    // Constructor for main branch (current usage)
    public CreateCommitRequest(int emotion_id, int intensity_level, String message) {
        this.emotion_id = emotion_id;
        this.branch_type = "main";
        this.user_quest_id = null;
        this.intensity_level = intensity_level;
        this.message = message;
    }

    // Constructor for quest branch (future usage)
    public CreateCommitRequest(int emotion_id, String branch_type, int user_quest_id, int intensity_level, String message) {
        this.emotion_id = emotion_id;
        this.branch_type = branch_type;
        this.user_quest_id = user_quest_id;
        this.intensity_level = intensity_level;
        this.message = message;
    }

    // Getters and Setters
    public int getEmotion_id() {
        return emotion_id;
    }

    public void setEmotion_id(int emotion_id) {
        this.emotion_id = emotion_id;
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
}
