package com.example.emotiondebugging.model.request;

import com.google.gson.annotations.SerializedName;

public class VoteRequest {
    @SerializedName("vote_type")
    private String voteType;

    public VoteRequest(String voteType) {
        this.voteType = voteType;
    }

    public String getVoteType() {
        return voteType;
    }
}