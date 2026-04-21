package com.example.emotiondebugging.model.request;

import com.google.gson.annotations.SerializedName;

public class CommitJournalRequest {

    @SerializedName("emotion")
    private String emotion;

    @SerializedName("intensity")
    private int intensity;

    @SerializedName("message")
    private String message;

    @SerializedName("branch")
    private String branch;

    @SerializedName("quest_id")
    private Integer questId; // Optional, only for quest branch

    public CommitJournalRequest(String emotion, int intensity, String message, String branch) {
        this.emotion = emotion;
        this.intensity = intensity;
        this.message = message;
        this.branch = branch;
    }

    public CommitJournalRequest(String emotion, int intensity, String message, String branch, Integer questId) {
        this.emotion = emotion;
        this.intensity = intensity;
        this.message = message;
        this.branch = branch;
        this.questId = questId;
    }

    // Getters and Setters
    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getQuestId() {
        return questId;
    }

    public void setQuestId(Integer questId) {
        this.questId = questId;
    }
}
