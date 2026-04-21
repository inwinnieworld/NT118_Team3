package com.example.emotiondebugging.model.response;

import com.example.emotiondebugging.model.GitGraphCommit;
import com.example.emotiondebugging.model.GitGraphMerge;

import java.util.List;

/**
 * Response model for Git Graph API
 * GET /api/gitjournal/graph
 */
public class GitGraphResponse {
    private boolean success;
    private GraphData data;
    private String message;

    public static class GraphData {
        private List<GitGraphCommit> commits;
        private List<GitGraphMerge> merges;
        private int total_commits;
        private int total_merges;

        // Getters and Setters
        public List<GitGraphCommit> getCommits() {
            return commits;
        }

        public void setCommits(List<GitGraphCommit> commits) {
            this.commits = commits;
        }

        public List<GitGraphMerge> getMerges() {
            return merges;
        }

        public void setMerges(List<GitGraphMerge> merges) {
            this.merges = merges;
        }

        public int getTotal_commits() {
            return total_commits;
        }

        public void setTotal_commits(int total_commits) {
            this.total_commits = total_commits;
        }

        public int getTotal_merges() {
            return total_merges;
        }

        public void setTotal_merges(int total_merges) {
            this.total_merges = total_merges;
        }
    }

    // Constructors
    public GitGraphResponse() {}

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public GraphData getData() {
        return data;
    }

    public void setData(GraphData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
