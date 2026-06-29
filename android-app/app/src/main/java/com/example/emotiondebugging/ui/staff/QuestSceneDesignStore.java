package com.example.emotiondebugging.ui.staff;

import com.example.emotiondebugging.model.request.QuestDraftRequest;

import java.util.ArrayList;
import java.util.List;

public final class QuestSceneDesignStore {
    public static String selectedNodeId;
    public static String backgroundUrl;
    public static String backgroundColor;
    public static List<QuestDraftRequest.QuestFlowNode> sceneNodes = new ArrayList<>();

    private QuestSceneDesignStore() { }

    public static void set(String nodeId, String imageUrl, String color,
                           List<QuestDraftRequest.QuestFlowNode> nodes) {
        selectedNodeId = nodeId;
        backgroundUrl = imageUrl == null ? "" : imageUrl;
        backgroundColor = color == null ? "#FFFFFF" : color;
        sceneNodes = nodes == null ? new ArrayList<>() : new ArrayList<>(nodes);
    }

    public static void clear() {
        selectedNodeId = null;
        backgroundUrl = null;
        backgroundColor = null;
        sceneNodes = new ArrayList<>();
    }
}
