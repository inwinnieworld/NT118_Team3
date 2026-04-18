package com.example.emotiondebugging.model.request;

public class CreateQuestRequest {
    private int errorTypeId;
    private String questTitle;
    private String questDescription;

    public CreateQuestRequest(int errorTypeId, String questTitle, String questDescription) {
        this.errorTypeId = errorTypeId;
        this.questTitle = questTitle;
        this.questDescription = questDescription;
    }

    public int getErrorTypeId() {
        return errorTypeId;
    }

    public String getQuestTitle() {
        return questTitle;
    }

    public String getQuestDescription() {
        return questDescription;
    }
}