package com.example.emotiondebugging.ui.gitgraph;

import com.example.emotiondebugging.model.GitGraphCommit;
import com.example.emotiondebugging.model.GitGraphMerge;

/**
 * Represents a node in the Git Graph
 * Can be either a Commit or a Merge
 */
public class GitGraphNode {
    
    public enum NodeType {
        COMMIT,
        MERGE
    }
    
    private NodeType type;
    private GitGraphCommit commit;
    private GitGraphMerge merge;
    
    // Position in graph
    private float x;
    private float y;
    
    // Visual properties
    private int color;
    private float radius;
    
    // For quest branches
    private Integer questId;
    private boolean isQuestStart;
    private boolean isQuestEnd;
    
    // Constructors
    private GitGraphNode(NodeType type) {
        this.type = type;
    }
    
    public static GitGraphNode fromCommit(GitGraphCommit commit) {
        GitGraphNode node = new GitGraphNode(NodeType.COMMIT);
        node.commit = commit;
        node.questId = commit.getUser_quest_id();
        return node;
    }
    
    public static GitGraphNode fromMerge(GitGraphMerge merge) {
        GitGraphNode node = new GitGraphNode(NodeType.MERGE);
        node.merge = merge;
        return node;
    }
    
    // Getters and Setters
    public NodeType getType() {
        return type;
    }
    
    public GitGraphCommit getCommit() {
        return commit;
    }
    
    public GitGraphMerge getMerge() {
        return merge;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public float getRadius() {
        return radius;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public Integer getQuestId() {
        return questId;
    }
    
    public void setQuestId(Integer questId) {
        this.questId = questId;
    }
    
    public boolean isQuestStart() {
        return isQuestStart;
    }
    
    public void setQuestStart(boolean questStart) {
        isQuestStart = questStart;
    }
    
    public boolean isQuestEnd() {
        return isQuestEnd;
    }
    
    public void setQuestEnd(boolean questEnd) {
        isQuestEnd = questEnd;
    }
    
    // Helper methods
    public boolean isCommitNode() {
        return type == NodeType.COMMIT;
    }
    
    public boolean isMergeNode() {
        return type == NodeType.MERGE;
    }
    
    public boolean isMainBranch() {
        return isCommitNode() && commit.isMainBranch();
    }
    
    public boolean isQuestBranch() {
        return isCommitNode() && commit.isQuestBranch();
    }
    
    public String getTimestamp() {
        if (isCommitNode()) {
            return commit.getCreated_at();
        } else if (isMergeNode()) {
            // For merge nodes, use merge_date as timestamp (format: YYYY-MM-DD)
            // Convert to full timestamp format for consistency
            return merge.getMerge_date() + "T23:59:59.999Z";
        }
        return "";
    }
}
