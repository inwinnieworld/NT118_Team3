package com.example.emotiondebugging.ui.staff;

import com.example.emotiondebugging.model.request.QuestDraftRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestPreviewStore {
    public static String questTitle = "";
    public static String backgroundUrl = "";
    public static String backgroundColor = "#FFFFFF";
    public static String backgroundSoundUrl = "";
    public static int backgroundSoundVolume = 35;
    public static List<QuestDraftRequest.QuestFlowNode> nodes;
    public static List<QuestDraftRequest.QuestFlowEdge> edges;

    public static void set(String title,
                           String bgUrl,
                           String bgColor,
                           String bgSoundUrl,
                           int bgSoundVolume,
                           List<QuestDraftRequest.QuestFlowNode> flowNodes,
                           List<QuestDraftRequest.QuestFlowEdge> flowEdges) {
        questTitle = title == null ? "" : title;
        backgroundUrl = bgUrl == null ? "" : bgUrl;
        backgroundColor = bgColor == null || bgColor.trim().isEmpty() ? "#FFFFFF" : bgColor;
        backgroundSoundUrl = bgSoundUrl == null ? "" : bgSoundUrl;
        backgroundSoundVolume = Math.max(0, Math.min(100, bgSoundVolume));
        nodes = flowNodes;
        edges = flowEdges;
    }

    public static Map<String, QuestDraftRequest.QuestFlowNode> nodeMap() {
        Map<String, QuestDraftRequest.QuestFlowNode> map = new HashMap<>();
        if (nodes == null) return map;
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            map.put(node.client_node_id, node);
        }
        return map;
    }
}
