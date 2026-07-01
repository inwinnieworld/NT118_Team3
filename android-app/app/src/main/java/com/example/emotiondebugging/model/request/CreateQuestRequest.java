package com.example.emotiondebugging.model.request;

public class CreateQuestRequest {
    private String problemId;
    private String questTitle;
    private String questDescription;

    public CreateQuestRequest(String problemId, String questTitle, String questDescription) {
        this.problemId = problemId;
        this.questTitle = questTitle;
        this.questDescription = questDescription;
    }

    public String getProblemId() {
        return problemId;
    }

    public String getQuestTitle() {
        return questTitle;
    }

    public String getQuestDescription() {
        return questDescription;
    }
}
