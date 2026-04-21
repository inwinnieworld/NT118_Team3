package com.example.emotiondebugging.model.response;

public class CreateCommitResponse {
    private boolean success;
    private String message;
    private CommitData data;

    // Inner classes for nested response structure
    public static class CommitData {
        private CommitInfo commit;
        private SeverityAlert alert;

        public CommitInfo getCommit() {
            return commit;
        }

        public void setCommit(CommitInfo commit) {
            this.commit = commit;
        }

        public SeverityAlert getAlert() {
            return alert;
        }

        public void setAlert(SeverityAlert alert) {
            this.alert = alert;
        }
    }

    public static class CommitInfo {
        private int commit_id;
        private int student_id;
        private int emotion_id;
        private String emotion_name;
        private String emotion_category;
        private String branch_type;
        private int intensity_level;
        private String message;
        private String created_at;

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

        public String getBranch_type() {
            return branch_type;
        }

        public void setBranch_type(String branch_type) {
            this.branch_type = branch_type;
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
    }

    public static class SeverityAlert {
        private boolean shouldAlert;
        private String alertType;
        private String message;
        private double severityScore;

        // Getters and Setters
        public boolean isShouldAlert() {
            return shouldAlert;
        }

        public void setShouldAlert(boolean shouldAlert) {
            this.shouldAlert = shouldAlert;
        }

        public String getAlertType() {
            return alertType;
        }

        public void setAlertType(String alertType) {
            this.alertType = alertType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public double getSeverityScore() {
            return severityScore;
        }

        public void setSeverityScore(double severityScore) {
            this.severityScore = severityScore;
        }
    }

    // Constructors
    public CreateCommitResponse() {}

    public CreateCommitResponse(boolean success, String message, CommitData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CommitData getData() {
        return data;
    }

    public void setData(CommitData data) {
        this.data = data;
    }
}
