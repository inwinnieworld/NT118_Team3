package com.example.emotiondebugging.model.response;

import java.util.List;

public class QuestTrendReportResponse {
    private List<QuestTrendAssignedItem> assigned;
    private List<QuestTrendCompletedItem> completed;

    public List<QuestTrendAssignedItem> getAssigned() {
        return assigned;
    }

    public List<QuestTrendCompletedItem> getCompleted() {
        return completed;
    }

    public static class QuestTrendAssignedItem {
        private String chart_date;
        private float total_assigned;

        public String getChart_date() {
            return chart_date;
        }

        public float getTotal_assigned() {
            return total_assigned;
        }
    }

    public static class QuestTrendCompletedItem {
        private String chart_date;
        private float total_completed;

        public String getChart_date() {
            return chart_date;
        }

        public float getTotal_completed() {
            return total_completed;
        }
    }
}