package com.example.emotiondebugging.ui.staff;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.data.repository.QuestBuilderRepository;
import com.example.emotiondebugging.model.domain.QuestEngine;
import com.example.emotiondebugging.model.domain.QuestProblem;
import com.example.emotiondebugging.model.request.QuestDraftRequest;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;

import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestBuilderViewModel extends ViewModel {

    private final QuestBuilderRepository repository = new QuestBuilderRepository();

    private final MutableLiveData<List<QuestEngine>> engines = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<QuestProblem>> problems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> aiMetadataSummary = new MutableLiveData<>("Chọn vấn đề");
    private final MutableLiveData<List<QuestDraftSummary>> drafts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<QuestDraftDetail> openedDraft = new MutableLiveData<>();
    private final MutableLiveData<List<QuestDraftRequest.QuestFlowNode>> nodes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<QuestDraftRequest.QuestFlowEdge>> edges = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> selectedNodeId = new MutableLiveData<>(null);
    private final MutableLiveData<String> selectedEdgeId = new MutableLiveData<>(null);
    private final MutableLiveData<String> currentFrameId = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> canvasConfigVisible = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> connectMode = new MutableLiveData<>(false);
    private final MutableLiveData<String> connectModeType = new MutableLiveData<>("sequential");
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Integer> savedQuestId = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> mediaUploading = new MutableLiveData<>(false);
    private final MutableLiveData<String> uploadedMediaUrl = new MutableLiveData<>();
    private final MutableLiveData<String> uploadedBackgroundSoundUrl = new MutableLiveData<>();

    private String backgroundUrl = "";
    private String backgroundColor = "#FFFFFF";
    private String backgroundSoundUrl = "";
    private int backgroundSoundVolume = 35;
    private String problemId;
    private String problemTitle = "";
    private List<String> aiTags = new ArrayList<>();
    private int intensityMin = 1;
    private int intensityMax = 5;
    private String therapeuticGoal = "grounding";
    private int estimatedDurationSeconds = 120;

    public LiveData<List<QuestEngine>> getEngines() { return engines; }
    public LiveData<List<QuestProblem>> getProblems() { return problems; }
    public LiveData<String> getAiMetadataSummary() { return aiMetadataSummary; }
    public LiveData<List<QuestDraftSummary>> getDrafts() { return drafts; }
    public LiveData<QuestDraftDetail> getOpenedDraft() { return openedDraft; }
    public LiveData<List<QuestDraftRequest.QuestFlowNode>> getNodes() { return nodes; }
    public LiveData<List<QuestDraftRequest.QuestFlowEdge>> getEdges() { return edges; }
    public LiveData<String> getSelectedNodeId() { return selectedNodeId; }
    public LiveData<String> getSelectedEdgeId() { return selectedEdgeId; }
    public LiveData<String> getCurrentFrameId() { return currentFrameId; }
    public LiveData<Boolean> getCanvasConfigVisible() { return canvasConfigVisible; }
    public LiveData<Boolean> getConnectMode() { return connectMode; }
    public LiveData<String> getConnectModeType() { return connectModeType; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getMessage() { return message; }
    public LiveData<Integer> getSavedQuestId() { return savedQuestId; }
    public LiveData<Boolean> getMediaUploading() { return mediaUploading; }
    public LiveData<String> getUploadedMediaUrl() { return uploadedMediaUrl; }
    public LiveData<String> getUploadedBackgroundSoundUrl() { return uploadedBackgroundSoundUrl; }

    public void uploadMedia(String token, File file, String mimeType) {
        uploadMedia(token, file, mimeType, false);
    }

    public void uploadBackgroundSound(String token, File file, String mimeType) {
        uploadMedia(token, file, mimeType, true);
    }

    private void uploadMedia(String token, File file, String mimeType, boolean backgroundSound) {
        mediaUploading.setValue(true);
        repository.uploadMedia(token, file, mimeType, new QuestBuilderRepository.RepositoryCallback<String>() {
            @Override public void onSuccess(String data, String text) {
                mediaUploading.setValue(false);
                if (backgroundSound) uploadedBackgroundSoundUrl.setValue(data);
                else uploadedMediaUrl.setValue(data);
                message.setValue("Media uploaded to server");
            }

            @Override public void onError(String text) {
                mediaUploading.setValue(false);
                message.setValue(text);
            }
        });
    }

    public String getBackgroundUrl() { return backgroundUrl; }
    public String getBackgroundColor() { return backgroundColor; }
    public String getBackgroundSoundUrl() { return backgroundSoundUrl; }
    public int getBackgroundSoundVolume() { return backgroundSoundVolume; }
    public String getProblemId() { return problemId; }
    public List<String> getAiTags() { return new ArrayList<>(aiTags); }
    public int getIntensityMin() { return intensityMin; }
    public int getIntensityMax() { return intensityMax; }
    public String getTherapeuticGoal() { return therapeuticGoal; }
    public int getEstimatedDurationSeconds() { return estimatedDurationSeconds; }

    public void loadEngines(String token) {
        loading.setValue(true);
        repository.getEngines(token, new QuestBuilderRepository.RepositoryCallback<List<QuestEngine>>() {
            @Override
            public void onSuccess(List<QuestEngine> data, String msg) {
                loading.postValue(false);
                List<QuestEngine> list = data == null ? new ArrayList<>() : new ArrayList<>(data);
                engines.postValue(ensureEssentialEngines(list));
            }

            @Override
            public void onError(String msg) {
                loading.postValue(false);
                engines.postValue(defaultEngines());
                message.postValue("Using local engine list because the server catalog is unavailable");
            }
        });
    }

    public void loadProblems(String token) {
        repository.getProblems(token, new QuestBuilderRepository.RepositoryCallback<List<QuestProblem>>() {
            @Override
            public void onSuccess(List<QuestProblem> data, String msg) {
                problems.postValue(data == null ? new ArrayList<>() : data);
            }

            @Override
            public void onError(String msg) {
                message.postValue(msg);
            }
        });
    }

    public void updateAiMetadata(QuestProblem problem, List<String> tags, int minimumIntensity,
                                 int maximumIntensity, String goal, int durationSeconds) {
        if (problem == null || !problem.isLeafNode() || problem.getTreeLevel() != 3) {
            message.setValue("Hãy chọn một vấn đề cụ thể ở cấp 3");
            return;
        }
        problemId = problem.getId();
        problemTitle = problem.getTitle();
        aiTags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        intensityMin = Math.max(1, Math.min(5, minimumIntensity));
        intensityMax = Math.max(intensityMin, Math.min(5, maximumIntensity));
        therapeuticGoal = goal == null || goal.trim().isEmpty() ? "grounding" : goal.trim();
        estimatedDurationSeconds = Math.max(10, Math.min(7200, durationSeconds));
        String shortProblem = problemTitle.length() > 18
                ? problemTitle.substring(0, 18) + "..." : problemTitle;
        aiMetadataSummary.setValue("Vấn đề: " + shortProblem);
        message.setValue("Đã chọn vấn đề cho AI mapping");
    }

    public void loadDrafts(String token) {
        loading.setValue(true);
        repository.getDrafts(token, new QuestBuilderRepository.RepositoryCallback<List<QuestDraftSummary>>() {
            @Override
            public void onSuccess(List<QuestDraftSummary> data, String msg) {
                loading.postValue(false);
                drafts.postValue(data == null ? new ArrayList<>() : data);
            }

            @Override
            public void onError(String msg) {
                loading.postValue(false);
                message.postValue(msg);
            }
        });
    }

    public void openDraft(String token, int versionId) {
        loading.setValue(true);
        repository.getDraftVersion(token, versionId,
                new QuestBuilderRepository.RepositoryCallback<QuestDraftDetail>() {
                    @Override
                    public void onSuccess(QuestDraftDetail data, String msg) {
                        loading.postValue(false);
                        if (data == null || data.flow == null) {
                            message.postValue("The selected draft has no flow data");
                            return;
                        }
                        restoreDraft(data);
                        openedDraft.postValue(data);
                        message.postValue("Draft opened: " + data.quest_title);
                    }

                    @Override
                    public void onError(String msg) {
                        loading.postValue(false);
                        message.postValue(msg);
                    }
                });
    }

    private void restoreDraft(QuestDraftDetail data) {
        savedQuestId.postValue(data.quest_id);
        problemId = data.problem_id;
        problemTitle = data.problem_title == null ? "" : data.problem_title;
        aiTags = data.ai_tags == null ? new ArrayList<>() : new ArrayList<>(data.ai_tags);
        intensityMin = data.intensity_min <= 0 ? 1 : data.intensity_min;
        intensityMax = data.intensity_max <= 0 ? 5 : data.intensity_max;
        therapeuticGoal = data.therapeutic_goal == null
                ? "grounding" : data.therapeutic_goal;
        estimatedDurationSeconds = data.estimated_duration_seconds == null
                ? 120 : data.estimated_duration_seconds;
        String shortProblem = problemTitle.length() > 18
                ? problemTitle.substring(0, 18) + "..." : problemTitle;
        aiMetadataSummary.postValue(problemTitle.isEmpty() ? "Chọn vấn đề" : "Vấn đề: " + shortProblem);

        Map<String, Object> canvas = data.canvas_config;
        backgroundUrl = mapString(canvas, "background_url", "");
        backgroundColor = mapString(canvas, "background_color", "#FFFFFF");
        backgroundSoundUrl = mapString(canvas, "background_sound_url", "");
        backgroundSoundVolume = mapInt(canvas, "background_sound_volume", 35);
        nodes.postValue(data.flow.nodes == null ? new ArrayList<>() : data.flow.nodes);
        edges.postValue(data.flow.edges == null ? new ArrayList<>() : data.flow.edges);
        selectedNodeId.postValue(null);
        selectedEdgeId.postValue(null);
        currentFrameId.postValue(null);
        canvasConfigVisible.postValue(false);
        connectMode.postValue(false);
        connectModeType.postValue("sequential");
    }

    private String mapString(Map<String, Object> map, String key, String fallback) {
        if (map == null || map.get(key) == null) return fallback;
        String value = String.valueOf(map.get(key));
        return "null".equals(value) ? fallback : value;
    }

    private int mapInt(Map<String, Object> map, String key, int fallback) {
        if (map == null || map.get(key) == null) return fallback;
        Object value = map.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        return parseIntOrDefault(String.valueOf(value), fallback);
    }

    public void addEngineAt(QuestEngine engine, float x, float y) {
        if ("parallel".equals(engine.getEngineSubtype()) || "sequential".equals(engine.getEngineSubtype())) {
            startConnectMode(engine.getEngineSubtype());
            return;
        }
        List<QuestDraftRequest.QuestFlowNode> current = copyNodes();
        QuestDraftRequest.QuestFlowNode node = new QuestDraftRequest.QuestFlowNode();
        int index = current.size() + 1;
        node.client_node_id = engine.getEngineSubtype() + "_" + System.currentTimeMillis();
        node.engine_type = engine.getEngineType();
        node.engine_subtype = engine.getEngineSubtype();
        node.display_name = defaultDisplayName(engine);
        boolean isFrame = "composite".equals(engine.getEngineSubtype());
        node.width = isFrame ? 220.0 : 190.0;
        node.height = isFrame ? 112.0 : 82.0;
        node.position_x = Math.max(8, x - node.width.floatValue() / 2f);
        node.position_y = Math.max(8, y - node.height.floatValue() / 2f);
        if (!isFrame) {
            String activeFrame = currentFrameId.getValue();
            node.parent_client_node_id = activeFrame == null ? frameAt(current, x, y) : activeFrame;
            if (activeFrame == null && node.parent_client_node_id != null) {
                QuestDraftRequest.QuestFlowNode parent = findNodeById(current, node.parent_client_node_id);
                if (parent != null) {
                    node.position_x = Math.max(8, x - (float) parent.position_x - node.width.floatValue() / 2f);
                    node.position_y = Math.max(8, y - (float) parent.position_y - node.height.floatValue() / 2f);
                }
            }
        }
        node.z_index = index;
        node.config = new HashMap<>();
        current.add(node);
        nodes.setValue(current);
        selectNode(node.client_node_id);
        if (node.parent_client_node_id != null) {
            message.setValue(node.display_name + " added to Frame");
        }
    }

    public void moveNode(String nodeId, float x, float y) {
        List<QuestDraftRequest.QuestFlowNode> current = copyNodes();
        for (QuestDraftRequest.QuestFlowNode node : current) {
            if (node.client_node_id.equals(nodeId)) {
                double oldX = node.position_x;
                double oldY = node.position_y;
                node.position_x = Math.max(0, x);
                node.position_y = Math.max(0, y);
                if ("composite".equals(node.engine_subtype)) {
                    // Children are edited inside the frame canvas, so moving the frame on the parent
                    // canvas should not shift the internal scene layout.
                } else {
                    double nodeWidth = node.width == null ? 190 : node.width;
                    double nodeHeight = node.height == null ? 82 : node.height;
                    String activeFrame = currentFrameId.getValue();
                    String newParentId = activeFrame == null
                            ? frameAt(current,
                            (float) (node.position_x + nodeWidth / 2f),
                            (float) (node.position_y + nodeHeight / 2f))
                            : activeFrame;
                    if (activeFrame == null && newParentId != null
                            && !newParentId.equals(node.parent_client_node_id)) {
                        QuestDraftRequest.QuestFlowNode parent = findNodeById(current, newParentId);
                        if (parent != null) {
                            node.position_x = Math.max(8, node.position_x - parent.position_x);
                            node.position_y = Math.max(8, node.position_y - parent.position_y);
                        }
                    }
                    node.parent_client_node_id = newParentId;
                }
                break;
            }
        }
        nodes.setValue(current);
    }

    public void selectNode(String nodeId) {
        selectedNodeId.setValue(nodeId);
        selectedEdgeId.setValue(null);
        canvasConfigVisible.setValue(false);
    }

    public void selectEdge(String edgeId) {
        selectedNodeId.setValue(null);
        selectedEdgeId.setValue(edgeId);
        canvasConfigVisible.setValue(false);
    }

    public void selectCanvas() {
        selectedNodeId.setValue(null);
        selectedEdgeId.setValue(null);
        canvasConfigVisible.setValue(true);
    }

    public void openSelectedFrame() {
        QuestDraftRequest.QuestFlowNode selected = getSelectedNode();
        if (selected == null || !"composite".equals(selected.engine_subtype)) {
            message.setValue("Select a frame first");
            return;
        }
        currentFrameId.setValue(selected.client_node_id);
        selectedNodeId.setValue(null);
        selectedEdgeId.setValue(null);
        canvasConfigVisible.setValue(false);
        message.setValue("Opened frame: " + selected.display_name);
    }

    public boolean exitCurrentFrame() {
        if (currentFrameId.getValue() == null) return false;
        currentFrameId.setValue(null);
        selectedNodeId.setValue(null);
        selectedEdgeId.setValue(null);
        canvasConfigVisible.setValue(false);
        message.setValue("Back to main quest flow");
        return true;
    }

    public void toggleConnectMode() {
        Boolean current = connectMode.getValue();
        String type = connectModeType.getValue();
        boolean next = current == null || !current || !"sequential".equals(type);
        if (next) {
            startConnectMode("sequential");
        } else {
            connectMode.setValue(false);
            message.setValue("Link mode is off");
        }
    }

    public void toggleParallelConnectMode() {
        Boolean current = connectMode.getValue();
        String type = connectModeType.getValue();
        boolean next = current == null || !current || !"parallel".equals(type);
        if (next) {
            startConnectMode("parallel");
        } else {
            connectMode.setValue(false);
            message.setValue("Link mode is off");
        }
    }

    private void startConnectMode(String type) {
        String normalized = "parallel".equals(type) ? "parallel" : "sequential";
        connectModeType.setValue(normalized);
        connectMode.setValue(true);
        message.setValue("parallel".equals(normalized)
                ? "Drag from engine A to engine B to create a parallel link"
                : "Drag from a source engine to a target engine to create a sequence arrow");
    }

    public void addSequentialEdge(String sourceNodeId, String targetNodeId) {
        if (sourceNodeId == null || targetNodeId == null || sourceNodeId.equals(targetNodeId)) return;
        List<QuestDraftRequest.QuestFlowEdge> current = copyEdges();
        for (QuestDraftRequest.QuestFlowEdge edge : current) {
            if (sourceNodeId.equals(edge.source_client_node_id)
                    && targetNodeId.equals(edge.target_client_node_id)) {
                message.setValue("This sequence arrow already exists");
                return;
            }
        }
        String linkType = "parallel".equals(connectModeType.getValue()) ? "parallel" : "sequential";
        QuestDraftRequest.QuestFlowEdge edge = new QuestDraftRequest.QuestFlowEdge();
        edge.client_edge_id = "edge_" + System.currentTimeMillis();
        edge.source_client_node_id = sourceNodeId;
        edge.target_client_node_id = targetNodeId;
        edge.flow_type = linkType;
        edge.sort_order = current.size() + 1;
        edge.config = new HashMap<>();
        if ("parallel".equals(linkType)) {
            edge.completion_condition = "A_OR_B";
            edge.config.put("completion_condition", "A_OR_B");
        } else {
            edge.config.put("transition_type", "delay");
            edge.config.put("delay_seconds", 3);
            edge.config.put("transition_effect", "fade");
        }
        current.add(edge);
        edges.setValue(current);
        connectMode.setValue(false);
        selectEdge(edge.client_edge_id);
        message.setValue("parallel".equals(linkType) ? "Parallel link created" : "Sequence arrow created");
    }

    private QuestDraftRequest.QuestFlowNode findNodeById(String nodeId) {
        List<QuestDraftRequest.QuestFlowNode> current = nodes.getValue();
        if (current == null) return null;
        return findNodeById(current, nodeId);
    }

    private QuestDraftRequest.QuestFlowNode findNodeById(List<QuestDraftRequest.QuestFlowNode> current, String nodeId) {
        for (QuestDraftRequest.QuestFlowNode node : current) {
            if (nodeId.equals(node.client_node_id)) return node;
        }
        return null;
    }

    public void removeSelectedNode() {
        String nodeId = selectedNodeId.getValue();
        if (nodeId == null) return;

        List<QuestDraftRequest.QuestFlowNode> currentNodes = copyNodes();
        List<String> removedIds = new ArrayList<>();
        collectNodeAndChildren(currentNodes, nodeId, removedIds);
        currentNodes.removeIf(node -> removedIds.contains(node.client_node_id));
        List<QuestDraftRequest.QuestFlowEdge> currentEdges = copyEdges();
        currentEdges.removeIf(edge -> removedIds.contains(edge.source_client_node_id)
                || removedIds.contains(edge.target_client_node_id));

        selectedNodeId.setValue(null);
        selectedEdgeId.setValue(null);
        canvasConfigVisible.setValue(false);
        edges.setValue(currentEdges);
        nodes.setValue(currentNodes);
        message.setValue("Engine deleted");
    }

    private void collectNodeAndChildren(List<QuestDraftRequest.QuestFlowNode> nodes,
                                        String nodeId,
                                        List<String> removedIds) {
        if (removedIds.contains(nodeId)) return;
        removedIds.add(nodeId);
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            if (nodeId.equals(node.parent_client_node_id)) {
                collectNodeAndChildren(nodes, node.client_node_id, removedIds);
            }
        }
    }

    public void removeEdge(String edgeId) {
        if (edgeId == null) return;
        List<QuestDraftRequest.QuestFlowEdge> current = copyEdges();
        boolean removed = current.removeIf(edge -> edgeId.equals(edge.client_edge_id));
        if (removed) {
            selectedEdgeId.setValue(null);
            edges.setValue(current);
            message.setValue("Flow link deleted");
        }
    }

    public void updateSelectedEdge(String transitionType, String delayValue, String effect) {
        String edgeId = selectedEdgeId.getValue();
        if (edgeId == null) return;
        List<QuestDraftRequest.QuestFlowEdge> current = copyEdges();
        for (QuestDraftRequest.QuestFlowEdge edge : current) {
            if (!edgeId.equals(edge.client_edge_id)) continue;
            if (edge.config == null) edge.config = new HashMap<>();
            String type = "immediate".equals(transitionType) ? "immediate" : "delay";
            edge.config.put("transition_type", type);
            edge.config.put("delay_seconds", "delay".equals(type)
                    ? clamp(parseIntOrDefault(delayValue, 3), 0, 300) : 0);
            edge.config.put("transition_effect", effect == null ? "none" : effect);
            break;
        }
        edges.setValue(current);
        message.setValue("Sequence transition updated");
    }

    public void updateSelectedParallelEdge(String completionCondition) {
        String edgeId = selectedEdgeId.getValue();
        if (edgeId == null) return;
        List<QuestDraftRequest.QuestFlowEdge> current = copyEdges();
        for (QuestDraftRequest.QuestFlowEdge edge : current) {
            if (!edgeId.equals(edge.client_edge_id)) continue;
            if (edge.config == null) edge.config = new HashMap<>();
            String condition = completionCondition == null || completionCondition.trim().isEmpty()
                    ? "A_OR_B" : completionCondition.trim();
            edge.completion_condition = condition;
            edge.config.put("completion_condition", condition);
            break;
        }
        edges.setValue(current);
        message.setValue("Parallel condition updated");
    }

    public QuestDraftRequest.QuestFlowEdge getSelectedEdge() {
        String edgeId = selectedEdgeId.getValue();
        if (edgeId == null) return null;
        List<QuestDraftRequest.QuestFlowEdge> current = edges.getValue();
        if (current == null) return null;
        for (QuestDraftRequest.QuestFlowEdge edge : current) {
            if (edgeId.equals(edge.client_edge_id)) return edge;
        }
        return null;
    }

    public void updateSelectedNode(String displayName, String primaryValue, String secondaryValue) {
        updateSelectedNode(displayName, primaryValue, secondaryValue, "none", "", "");
    }

    public void updateSelectedNode(String displayName, String primaryValue, String secondaryValue,
                                   String attachedEngineType, String attachedPrimaryValue,
                                   String attachedSecondaryValue) {
        String nodeId = selectedNodeId.getValue();
        if (nodeId == null) return;

        List<QuestDraftRequest.QuestFlowNode> current = copyNodes();
        String appliedName = null;
        for (QuestDraftRequest.QuestFlowNode node : current) {
            if (node.client_node_id.equals(nodeId)) {
                if (displayName != null && !displayName.trim().isEmpty()) {
                    node.display_name = displayName.trim();
                }
                appliedName = node.display_name;
                Map<String, Object> previousConfig = node.config;
                node.config = buildConfig(
                        node.engine_subtype,
                        primaryValue,
                        secondaryValue,
                        attachedEngineType,
                        attachedPrimaryValue,
                        attachedSecondaryValue
                );
                copySceneLayout(previousConfig, node.config);
                break;
            }
        }
        nodes.setValue(current);
        if (appliedName != null) {
            message.setValue("Applied configuration for " + appliedName);
        }
    }

    private void copySceneLayout(Map<String, Object> source, Map<String, Object> target) {
        if (source == null || target == null) return;
        for (String key : new String[]{"scene_x", "scene_y", "scene_width", "scene_height"}) {
            if (source.containsKey(key)) target.put(key, source.get(key));
        }
    }

    public void updateCanvasConfig(String backgroundUrl, String backgroundColor,
                                   String soundUrl, String soundVolume) {
        this.backgroundUrl = backgroundUrl == null ? "" : backgroundUrl.trim();
        this.backgroundColor = backgroundColor == null || backgroundColor.trim().isEmpty()
                ? "#FFFFFF" : backgroundColor.trim();
        this.backgroundSoundUrl = soundUrl == null ? "" : soundUrl.trim();
        this.backgroundSoundVolume = clamp(parseIntOrDefault(soundVolume, 35), 0, 100);
        canvasConfigVisible.setValue(true);
        message.setValue("Quest background updated");
    }

    public void updateSelectedImageLayout(String position, String widthValue, String heightValue) {
        String nodeId = selectedNodeId.getValue();
        if (nodeId == null) return;
        List<QuestDraftRequest.QuestFlowNode> current = copyNodes();
        for (QuestDraftRequest.QuestFlowNode node : current) {
            if (nodeId.equals(node.client_node_id) && "image".equals(node.engine_subtype)) {
                if (node.config == null) node.config = new HashMap<>();
                node.config.put("position", position == null || position.isEmpty() ? "center" : position);
                node.config.put("width", clamp(parseIntOrDefault(widthValue, 240), 40, 1200));
                node.config.put("height", clamp(parseIntOrDefault(heightValue, 180), 40, 1200));
                break;
            }
        }
        nodes.setValue(current);
    }

    public void updateSelectedSceneLayout(float x, float y, float width, float height) {
        String nodeId = selectedNodeId.getValue();
        if (nodeId == null) return;
        List<QuestDraftRequest.QuestFlowNode> current = copyNodes();
        for (QuestDraftRequest.QuestFlowNode node : current) {
            if (!nodeId.equals(node.client_node_id)) continue;
            if (node.config == null) node.config = new HashMap<>();
            node.config.put("scene_x", Math.max(0f, x));
            node.config.put("scene_y", Math.max(0f, y));
            node.config.put("scene_width", Math.max(40f, width));
            node.config.put("scene_height", Math.max(32f, height));
            break;
        }
        nodes.setValue(current);
        message.setValue("Scene position applied");
    }

    private String frameAt(List<QuestDraftRequest.QuestFlowNode> current, float x, float y) {
        for (int i = current.size() - 1; i >= 0; i--) {
            QuestDraftRequest.QuestFlowNode candidate = current.get(i);
            if (!"composite".equals(candidate.engine_subtype)) continue;
            double width = candidate.width == null ? 220 : candidate.width;
            double height = candidate.height == null ? 112 : candidate.height;
            if (x >= candidate.position_x && x <= candidate.position_x + width
                    && y >= candidate.position_y && y <= candidate.position_y + height) {
                return candidate.client_node_id;
            }
        }
        return null;
    }

    public QuestDraftRequest.QuestFlowNode getSelectedNode() {
        String nodeId = selectedNodeId.getValue();
        if (nodeId == null) return null;
        List<QuestDraftRequest.QuestFlowNode> current = nodes.getValue();
        if (current == null) return null;
        for (QuestDraftRequest.QuestFlowNode node : current) {
            if (nodeId.equals(node.client_node_id)) return node;
        }
        return null;
    }

    public void saveDraft(String token, String title, String description, int level) {
        List<QuestDraftRequest.QuestFlowNode> currentNodes = nodes.getValue();
        if (title == null || title.trim().isEmpty()) {
            message.setValue("Enter a quest title before saving");
            return;
        }
        if (currentNodes == null || currentNodes.isEmpty()) {
            message.setValue("Drag at least one engine onto the white canvas");
            return;
        }
        if (problemId == null || problemId.trim().isEmpty()) {
            message.setValue("Hãy chọn đủ 3 cấp vấn đề trước khi lưu");
            return;
        }

        QuestDraftRequest request = new QuestDraftRequest();
        request.quest_id = savedQuestId.getValue();
        request.quest_title = title.trim();
        request.quest_description = description == null ? "" : description.trim();
        request.quest_level = Math.max(1, Math.min(level, 5));
        request.problem_id = problemId;
        request.canvas_config = buildCanvasConfig();
        request.flow = new QuestDraftRequest.QuestFlow();
        request.flow.nodes = currentNodes;
        request.flow.edges = edges.getValue() == null ? new ArrayList<>() : edges.getValue();

        loading.setValue(true);
        repository.saveDraft(token, request, new QuestBuilderRepository.RepositoryCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data, String msg) {
                loading.postValue(false);
                Integer questId = extractInt(data, "quest_id");
                if (questId != null) savedQuestId.postValue(questId);
                message.postValue(msg);
            }

            @Override
            public void onError(String msg) {
                loading.postValue(false);
                message.postValue(msg);
            }
        });
    }

    public void submitReview(String token) {
        Integer questId = savedQuestId.getValue();
        if (questId == null) {
            message.setValue("Save the draft before submitting for review");
            return;
        }

        loading.setValue(true);
        repository.submitReview(token, questId, new QuestBuilderRepository.RepositoryCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> data, String msg) {
                loading.postValue(false);
                message.postValue(msg);
            }

            @Override
            public void onError(String msg) {
                loading.postValue(false);
                message.postValue(msg);
            }
        });
    }

    private List<QuestDraftRequest.QuestFlowNode> copyNodes() {
        List<QuestDraftRequest.QuestFlowNode> current = nodes.getValue();
        return current == null ? new ArrayList<>() : new ArrayList<>(current);
    }

    private List<QuestDraftRequest.QuestFlowEdge> copyEdges() {
        List<QuestDraftRequest.QuestFlowEdge> current = edges.getValue();
        return current == null ? new ArrayList<>() : new ArrayList<>(current);
    }

    private String defaultDisplayName(QuestEngine engine) {
        String subtype = engine.getEngineSubtype();
        if ("sequential".equals(subtype)) return "Sequence Arrow";
        if ("parallel".equals(subtype)) return "Parallel Link";
        if ("composite".equals(subtype)) return "Frame";
        if ("quest".equals(subtype)) return "Sub Quest";
        if ("text_input".equals(subtype)) return "Text Input";
        if (subtype == null || subtype.isEmpty()) return "Engine";
        return subtype.substring(0, 1).toUpperCase() + subtype.substring(1).replace("_", " ");
    }

    private List<QuestEngine> ensureEssentialEngines(List<QuestEngine> source) {
        List<QuestEngine> list = new ArrayList<>();
        if (source != null) {
            for (QuestEngine engine : source) {
                if (!"quest".equals(engine.getEngineSubtype())) list.add(engine);
            }
        }
        addEngineIfMissing(list, "flow", "sequential", "->");
        addEngineIfMissing(list, "flow", "parallel", "=");
        addEngineIfMissing(list, "flow", "composite", "[]");
        addEngineIfMissing(list, "media", "image", "IMG");
        addEngineIfMissing(list, "media", "video", "VID");
        addEngineIfMissing(list, "media", "audio", "AUD");
        addEngineIfMissing(list, "input", "gesture", "TAP");
        addEngineIfMissing(list, "input", "sensor", "SNS");
        addEngineIfMissing(list, "input", "voice", "MIC");
        addEngineIfMissing(list, "input", "text_input", "IN");
        addEngineIfMissing(list, "output", "text", "TXT");
        addEngineIfMissing(list, "output", "timer", "60");
        return list;
    }

    private List<QuestEngine> defaultEngines() {
        return ensureEssentialEngines(new ArrayList<>());
    }

    private void addEngineIfMissing(List<QuestEngine> list, String type, String subtype, String symbol) {
        for (QuestEngine engine : list) {
            if (subtype.equals(engine.getEngineSubtype())) return;
        }
        list.add(new QuestEngine(type, subtype, symbol));
    }

    private Map<String, Object> buildConfig(String subtype, String primaryValue, String secondaryValue,
                                            String attachedEngineType, String attachedPrimaryValue,
                                            String attachedSecondaryValue) {
        Map<String, Object> config = new HashMap<>();
        String primary = primaryValue == null ? "" : primaryValue.trim();
        String secondary = secondaryValue == null ? "" : secondaryValue.trim();
        String attachedType = attachedEngineType == null ? "none" : attachedEngineType.trim();
        String attachedPrimary = attachedPrimaryValue == null ? "" : attachedPrimaryValue.trim();
        String attachedSecondary = attachedSecondaryValue == null ? "" : attachedSecondaryValue.trim();

        switch (subtype) {
            case "text":
                config.put("text", primary);
                putIntIfPresent(config, "size", secondary, 22);
                config.put("position", "top");
                config.put("animation", "fade_in");
                config.put("bold", true);
                addAttachedEngineConfig(config, attachedType, attachedPrimary, attachedSecondary);
                break;
            case "timer":
                putIntIfPresent(config, "duration_seconds", primary, 60);
                putIntIfPresent(config, "size", secondary, 48);
                config.put("position", "center");
                break;
            case "image":
                config.put("asset_url", primary);
                config.put("position", secondary.isEmpty() ? "center" : secondary);
                addAttachedEngineConfig(config, attachedType, attachedPrimary, attachedSecondary);
                break;
            case "video":
                config.put("asset_url", primary);
                break;
            case "audio":
                config.put("asset_url", primary);
                putIntIfPresent(config, "volume", secondary, 80);
                config.put("speed", 1);
                break;
            case "gesture":
                config.put("gesture_type", primary);
                addGestureSpecificConfig(config, primary, secondary);
                break;
            case "sensor":
                config.put("sensor_action", primary);
                putIntIfPresent(config, "duration_seconds", secondary, 3);
                break;
            case "voice":
                config.put("mode", primary);
                putIntIfPresent(config, "duration_seconds", secondary, 10);
                break;
            case "text_input":
                config.put("placeholder", primary);
                putIntIfPresent(config, "word_limit", secondary, 60);
                break;
            case "parallel":
                config.put("completion_condition", primary);
                break;
            case "composite":
                config.put("background_url", primary);
                break;
            case "quest":
                config.put("nested_title", primary);
                config.put("note", secondary);
                config.put("nested_flow", new HashMap<>());
                break;
            default:
                config.put("value", primary);
                config.put("note", secondary);
                break;
        }
        return config;
    }

    private void addAttachedEngineConfig(Map<String, Object> config, String type,
                                         String primary, String secondary) {
        if (type == null || type.isEmpty() || "none".equals(type)) return;

        Map<String, Object> attached = new HashMap<>();
        attached.put("engine_subtype", type);

        switch (type) {
            case "timer":
                putIntIfPresent(attached, "duration_seconds", primary, 60);
                break;
            case "gesture":
                attached.put("gesture_type", primary);
                addGestureSpecificConfig(attached, primary, secondary);
                break;
            case "sensor":
                attached.put("sensor_action", primary);
                putIntIfPresent(attached, "duration_seconds", secondary, 3);
                break;
            case "voice":
                attached.put("mode", primary);
                putIntIfPresent(attached, "duration_seconds", secondary, 10);
                break;
            case "text_input":
                attached.put("placeholder", primary);
                putIntIfPresent(attached, "word_limit", secondary, 60);
                break;
            default:
                attached.put("value", primary);
                attached.put("note", secondary);
                break;
        }

        config.put("attached_engine", attached);
    }

    private void addGestureSpecificConfig(Map<String, Object> config, String gestureType, String value) {
        if ("tap".equals(gestureType)) {
            config.put("action_label", value == null ? "" : value.trim());
        } else if ("spam_tap".equals(gestureType)) {
            putIntIfPresent(config, "required_count", value, 2);
        } else if ("swipe".equals(gestureType)) {
            config.put("direction", value);
        } else if ("hold".equals(gestureType)) {
            putIntIfPresent(config, "duration_seconds", value, 1);
        }
    }

    private void putIntIfPresent(Map<String, Object> config, String key, String value, int fallback) {
        if (value == null || value.trim().isEmpty()) return;
        config.put(key, parseIntOrDefault(value, fallback));
    }

    private Map<String, Object> buildCanvasConfig() {
        Map<String, Object> canvas = new HashMap<>();
        Map<String, Object> viewport = new HashMap<>();
        viewport.put("width", 1280);
        viewport.put("height", 720);
        canvas.put("zoom", 1);
        canvas.put("viewport", viewport);
        canvas.put("background_url", backgroundUrl);
        canvas.put("background_color", backgroundColor);
        canvas.put("background_sound_url", backgroundSoundUrl);
        canvas.put("background_sound_volume", backgroundSoundVolume);
        return canvas;
    }

    private int parseIntOrDefault(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private Integer extractInt(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) return null;
        Object value = data.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }
}
