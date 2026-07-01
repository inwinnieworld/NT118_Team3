package com.example.emotiondebugging.model.domain;

public class QuestProblem {
    public String id;
    public String title;
    public String parent_id;
    public int tree_level;
    public boolean is_leaf_node;

    public String getId() {
        return id == null ? "" : id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public String getParentId() {
        return parent_id;
    }

    public int getTreeLevel() {
        return tree_level;
    }

    public boolean isLeafNode() {
        return is_leaf_node;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
