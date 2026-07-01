package com.example.emotiondebugging.ui.staff;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.repository.QuestBuilderRepository;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.QuestDraftRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.io.File;

public class QuestPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_RUN_ID = "quest_run_id";
    public static final String EXTRA_RUN_TOKEN = "quest_run_token";

    private FrameLayout previewRoot;
    private FrameLayout parallelLayer;
    private View previewContent;
    private TextView tvPreviewTitle;
    private TextView tvMain;
    private TextView tvHint;
    private TextView tvStepCounter;
    private ImageView imgBackground;
    private ImageView imgPreview;
    private VideoView videoPreview;
    private EditText etPreviewInput;
    private TextWatcher inputWordLimitWatcher;
    private Button btnCompleteNode;
    private Button btnClosePreview;

    private List<QuestDraftRequest.QuestFlowNode> orderedNodes = new ArrayList<>();
    private Map<String, QuestDraftRequest.QuestFlowNode> nodeMap;
    private final Map<String, List<String>> frameCompletionsByTerminal = new HashMap<>();
    private int currentIndex = 0;
    private CountDownTimer timer;
    private MediaPlayer audioPlayer;
    private MediaPlayer backgroundMusicPlayer;
    private MediaRecorder voiceRecorder;
    private File voiceTempFile;
    private Runnable voicePollRunnable;
    private Runnable voiceStopRunnable;
    private Runnable pendingVoiceCompletion;
    private Map<String, Object> pendingVoiceConfig;
    private SensorManager sensorManager;
    private final List<SensorEventListener> activeSensorListeners = new ArrayList<>();
    private final Handler transitionHandler = new Handler(Looper.getMainLooper());
    private boolean transitionScheduled;
    private float touchStartX;
    private float touchStartY;
    private long touchStartTime;
    private int tapCount;
    private boolean gestureCompleted;
    private final List<CountDownTimer> parallelTimers = new ArrayList<>();
    private final List<MediaPlayer> parallelPlayers = new ArrayList<>();
    private final List<VideoView> parallelVideos = new ArrayList<>();
    private Integer nextIndexOverride;
    private final QuestBuilderRepository runRepository = new QuestBuilderRepository();
    private int runId;
    private String runToken = "";
    private boolean runFinished;
    private final long runStartedAtMs = System.currentTimeMillis();
    private final Set<String> completedNodeIds = new HashSet<>();
    private final ActivityResultLauncher<String> recordAudioPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                Map<String, Object> config = pendingVoiceConfig;
                Runnable completion = pendingVoiceCompletion;
                pendingVoiceConfig = null;
                pendingVoiceCompletion = null;
                if (completion == null) return;
                if (granted) beginVoiceCapture(config, completion);
                else {
                    Toast.makeText(this, "Microphone permission denied; voice step skipped",
                            Toast.LENGTH_LONG).show();
                    completion.run();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_preview);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        runId = getIntent().getIntExtra(EXTRA_RUN_ID, 0);
        runToken = getIntent().getStringExtra(EXTRA_RUN_TOKEN);
        if (runToken == null) runToken = "";
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                closeRun();
            }
        });
        loadFlow();
        renderCurrentNode();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBackgroundMusic();
    }

    @Override
    protected void onStop() {
        stopPreviewExecution();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopPreviewExecution();
        super.onDestroy();
    }

    private void stopPreviewExecution() {
        if (timer != null) timer.cancel();
        timer = null;
        transitionHandler.removeCallbacksAndMessages(null);
        transitionScheduled = false;
        if (previewRoot != null) previewRoot.setOnTouchListener(null);
        if (tvMain != null) tvMain.setOnTouchListener(null);
        if (imgPreview != null) imgPreview.setOnTouchListener(null);
        if (videoPreview != null) videoPreview.stopPlayback();
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
        releaseBackgroundMusic();
        stopVoiceCapture(false);
        if (sensorManager != null) {
            for (SensorEventListener listener : new ArrayList<>(activeSensorListeners)) {
                sensorManager.unregisterListener(listener);
            }
        }
        activeSensorListeners.clear();
        pendingVoiceConfig = null;
        pendingVoiceCompletion = null;
        stopParallelResources();
    }

    private void initViews() {
        previewRoot = findViewById(R.id.previewRoot);
        parallelLayer = findViewById(R.id.parallelLayer);
        previewContent = findViewById(R.id.previewContent);
        tvPreviewTitle = findViewById(R.id.tvPreviewTitle);
        tvMain = findViewById(R.id.tvMain);
        tvHint = findViewById(R.id.tvHint);
        tvStepCounter = findViewById(R.id.tvStepCounter);
        imgBackground = findViewById(R.id.imgBackground);
        imgPreview = findViewById(R.id.imgPreview);
        videoPreview = findViewById(R.id.videoPreview);
        etPreviewInput = findViewById(R.id.etPreviewInput);
        btnCompleteNode = findViewById(R.id.btnCompleteNode);
        btnClosePreview = findViewById(R.id.btnClosePreview);

        btnClosePreview.setOnClickListener(v -> closeRun());
        btnCompleteNode.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);
        tvStepCounter.setVisibility(View.GONE);
    }

    private void loadFlow() {
        tvPreviewTitle.setText(QuestPreviewStore.questTitle == null || QuestPreviewStore.questTitle.isEmpty()
                ? "Quest Preview" : QuestPreviewStore.questTitle);

        try {
            previewRoot.setBackgroundColor(Color.parseColor(QuestPreviewStore.backgroundColor));
        } catch (Exception ignored) {
            previewRoot.setBackgroundColor(Color.WHITE);
        }

        if (QuestPreviewStore.backgroundUrl != null && !QuestPreviewStore.backgroundUrl.isEmpty()) {
            imgBackground.setVisibility(View.VISIBLE);
            Glide.with(this).load(RetrofitClient.resolveMediaUrl(QuestPreviewStore.backgroundUrl)).into(imgBackground);
        } else {
            imgBackground.setVisibility(View.GONE);
        }
        startBackgroundMusic();

        nodeMap = QuestPreviewStore.nodeMap();
        orderedNodes = buildSequentialOrder();
        if (orderedNodes.isEmpty() && QuestPreviewStore.nodes != null) {
            orderedNodes = new ArrayList<>(QuestPreviewStore.nodes);
            orderedNodes.sort(Comparator.comparingInt(node -> node.z_index));
        }
    }

    private List<QuestDraftRequest.QuestFlowNode> buildSequentialOrder() {
        List<QuestDraftRequest.QuestFlowNode> result = new ArrayList<>();
        if (QuestPreviewStore.nodes == null || QuestPreviewStore.nodes.isEmpty()) return result;
        Set<String> targets = new HashSet<>();
        if (QuestPreviewStore.edges != null) {
            for (QuestDraftRequest.QuestFlowEdge edge : QuestPreviewStore.edges) {
                if ("sequential".equals(edge.flow_type) || "parallel".equals(edge.flow_type)) {
                    targets.add(edge.target_client_node_id);
                }
            }
        }

        QuestDraftRequest.QuestFlowNode start = null;
        for (QuestDraftRequest.QuestFlowNode node : QuestPreviewStore.nodes) {
            if (node.parent_client_node_id == null && !targets.contains(node.client_node_id)) {
                start = node;
                break;
            }
        }
        if (start == null) start = QuestPreviewStore.nodes.get(0);

        Set<String> visited = new HashSet<>();
        QuestDraftRequest.QuestFlowNode current = start;
        while (current != null && !visited.contains(current.client_node_id)) {
            visited.add(current.client_node_id);
            result.add(current);
            QuestDraftRequest.QuestFlowEdge parallelEdge = outgoingParallelEdge(current.client_node_id);
            current = parallelEdge == null
                    ? nextSequentialNode(current.client_node_id)
                    : nextAfterParallelEdge(parallelEdge);
        }
        return result;
    }

    private QuestDraftRequest.QuestFlowNode nextSequentialNode(String nodeId) {
        if (QuestPreviewStore.edges == null) return null;
        for (QuestDraftRequest.QuestFlowEdge edge : QuestPreviewStore.edges) {
            if ("sequential".equals(edge.flow_type) && nodeId.equals(edge.source_client_node_id)) {
                return nodeMap.get(edge.target_client_node_id);
            }
        }
        return null;
    }

    private QuestDraftRequest.QuestFlowEdge outgoingParallelEdge(String nodeId) {
        if (QuestPreviewStore.edges == null) return null;
        for (QuestDraftRequest.QuestFlowEdge edge : QuestPreviewStore.edges) {
            if ("parallel".equals(edge.flow_type) && nodeId.equals(edge.source_client_node_id)) {
                return edge;
            }
        }
        return null;
    }

    private QuestDraftRequest.QuestFlowNode nextAfterParallelEdge(QuestDraftRequest.QuestFlowEdge edge) {
        if (edge == null) return null;
        String nextSource = firstSequentialOutgoingTarget(edge.source_client_node_id);
        String nextTarget = firstSequentialOutgoingTarget(edge.target_client_node_id);
        String nextId = nextSource != null ? nextSource : nextTarget;
        return nextId == null ? null : nodeMap.get(nextId);
    }

    private void renderCurrentNode() {
        if (timer != null) timer.cancel();
        previewRoot.setOnTouchListener(null);
        tvMain.setOnTouchListener(null);
        tvMain.setOnClickListener(null);
        tvMain.setClickable(false);
        resetGestureEffect(tvMain);
        imgPreview.setOnTouchListener(null);
        imgPreview.setOnClickListener(null);
        imgPreview.setClickable(false);
        resetGestureEffect(imgPreview);
        videoPreview.setVisibility(View.GONE);
        videoPreview.stopPlayback();
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
        etPreviewInput.setOnEditorActionListener(null);
        if (inputWordLimitWatcher != null) {
            etPreviewInput.removeTextChangedListener(inputWordLimitWatcher);
            inputWordLimitWatcher = null;
        }
        etPreviewInput.setText("");
        etPreviewInput.setEnabled(true);
        transitionScheduled = false;
        resetTransitionEffect();
        nextIndexOverride = null;
        stopParallelResources();
        parallelLayer.removeAllViews();
        parallelLayer.setVisibility(View.GONE);
        previewContent.setVisibility(View.VISIBLE);
        imgPreview.setVisibility(View.GONE);
        etPreviewInput.setVisibility(View.GONE);
        tvMain.setText("");
        tvHint.setVisibility(View.GONE);
        btnCompleteNode.setVisibility(View.GONE);
        btnCompleteNode.setOnClickListener(null);
        resetGestureEffect(btnCompleteNode);

        if (orderedNodes.isEmpty()) {
            Toast.makeText(this, "No engine to preview", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentIndex >= orderedNodes.size()) {
            completeRun();
            return;
        }

        QuestDraftRequest.QuestFlowNode node = orderedNodes.get(currentIndex);
        applyFrameBackground(node);
        recordEvent(node.client_node_id, "node_started");
        renderNode(node);
        previewRoot.post(() -> applyScenePlacement(node));
    }

    private void renderNode(QuestDraftRequest.QuestFlowNode node) {
        Map<String, Object> config = node.config;
        QuestDraftRequest.QuestFlowEdge parallelEdge = outgoingParallelEdge(node.client_node_id);
        if (parallelEdge != null) {
            renderParallelLink(parallelEdge);
            return;
        }
        String subtype = node.engine_subtype;
        switch (subtype) {
            case "composite":
                expandFrameAtCurrentIndex(node);
                break;
            case "text":
                tvMain.setText(stringValue(config, "text", node.display_name));
                tvMain.setTextSize(intValue(config, "size", 22));
                if (!renderAttachedEngine(config, tvMain)) advancePastPassiveOutput();
                break;
            case "timer":
                startTimer(
                        intValue(config, "duration_seconds", 5),
                        intValue(config, "size", 48),
                        false
                );
                break;
            case "image":
                renderMedia(config);
                if (!renderAttachedEngine(config, imgPreview)) advancePastPassiveOutput();
                break;
            case "video":
                renderVideo(config);
                break;
            case "audio":
                renderAudio(config);
                break;
            case "gesture":
                renderGesture(config);
                break;
            case "sensor":
                startSensor(config, this::goNext);
                break;
            case "voice":
                startVoice(config, this::goNext);
                break;
            case "text_input":
                configureTextInput(config, this::goNext);
                break;
            case "parallel":
                renderParallelGroup(node);
                break;
            case "quest":
                Toast.makeText(this, "Nested quest execution is not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void expandFrameAtCurrentIndex(QuestDraftRequest.QuestFlowNode frame) {
        List<QuestDraftRequest.QuestFlowNode> children = buildFrameOrder(frame.client_node_id);
        if (children.isEmpty()) {
            goNext();
            return;
        }
        String terminalLeafId = terminalLeafId(children.get(children.size() - 1), new HashSet<>());
        frameCompletionsByTerminal.computeIfAbsent(terminalLeafId, ignored -> new ArrayList<>())
                .add(frame.client_node_id);
        orderedNodes.remove(currentIndex);
        orderedNodes.addAll(currentIndex, children);
        renderCurrentNode();
    }

    private String terminalLeafId(QuestDraftRequest.QuestFlowNode node, Set<String> visitedFrames) {
        if (node == null || !"composite".equals(node.engine_subtype)
                || !visitedFrames.add(node.client_node_id)) {
            return node == null ? "" : node.client_node_id;
        }
        List<QuestDraftRequest.QuestFlowNode> children = buildFrameOrder(node.client_node_id);
        if (children.isEmpty()) return node.client_node_id;
        return terminalLeafId(children.get(children.size() - 1), visitedFrames);
    }

    private List<QuestDraftRequest.QuestFlowNode> buildFrameOrder(String frameId) {
        List<QuestDraftRequest.QuestFlowNode> children = new ArrayList<>();
        if (QuestPreviewStore.nodes == null) return children;
        QuestDraftRequest.QuestFlowNode frame = nodeMap.get(frameId);
        String terminalNodeId = frame == null ? null : frame.terminal_client_node_id;
        for (QuestDraftRequest.QuestFlowNode node : QuestPreviewStore.nodes) {
            if (frameId.equals(node.parent_client_node_id)) children.add(node);
        }
        if (children.isEmpty()) return children;

        Set<String> childIds = new HashSet<>();
        Set<String> childTargets = new HashSet<>();
        for (QuestDraftRequest.QuestFlowNode child : children) childIds.add(child.client_node_id);
        if (QuestPreviewStore.edges != null) {
            for (QuestDraftRequest.QuestFlowEdge edge : QuestPreviewStore.edges) {
                if (("sequential".equals(edge.flow_type) || "parallel".equals(edge.flow_type))
                        && childIds.contains(edge.source_client_node_id)
                        && childIds.contains(edge.target_client_node_id)) {
                    childTargets.add(edge.target_client_node_id);
                }
            }
        }

        QuestDraftRequest.QuestFlowNode start = children.get(0);
        for (QuestDraftRequest.QuestFlowNode candidate : children) {
            if (!childTargets.contains(candidate.client_node_id)) {
                start = candidate;
                break;
            }
        }

        List<QuestDraftRequest.QuestFlowNode> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        QuestDraftRequest.QuestFlowNode current = start;
        while (current != null && frameId.equals(current.parent_client_node_id)
                && !visited.contains(current.client_node_id)) {
            visited.add(current.client_node_id);
            result.add(current);
            // The server persists the terminal child for a frame. Stop there even if
            // malformed or stale edge data contains another outgoing connection.
            if (current.client_node_id.equals(terminalNodeId)) break;
            QuestDraftRequest.QuestFlowEdge parallelEdge = outgoingParallelEdge(current.client_node_id);
            current = parallelEdge == null
                    ? nextSequentialNode(current.client_node_id)
                    : nextAfterParallelEdge(parallelEdge);
        }
        return result;
    }

    private void renderMedia(Map<String, Object> config) {
        String url = stringValue(config, "asset_url", "");
        if (!url.isEmpty()) {
            int width = intValue(config, "width", 240);
            int height = intValue(config, "height", 180);
            ViewGroup.LayoutParams params = imgPreview.getLayoutParams();
            params.width = dp(width);
            params.height = dp(height);
            imgPreview.setLayoutParams(params);
            imgPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(RetrofitClient.resolveMediaUrl(url)).into(imgPreview);
            applyImagePosition(stringValue(config, "position", "center"));
        }
    }

    private void applyScenePlacement(QuestDraftRequest.QuestFlowNode node) {
        if (node == null || node.config == null || node.config.get("scene_x") == null) return;
        View target;
        switch (node.engine_subtype) {
            case "image": target = imgPreview; break;
            case "video": target = videoPreview; break;
            case "text_input": target = etPreviewInput; break;
            case "gesture": target = btnCompleteNode.getVisibility() == View.VISIBLE
                    ? btnCompleteNode : null; break;
            default: target = tvMain; break;
        }
        if (target == null || target.getVisibility() != View.VISIBLE) return;

        float scaleX = previewRoot.getWidth() / QuestSceneCanvasView.SCENE_WIDTH;
        float scaleY = previewRoot.getHeight() / QuestSceneCanvasView.SCENE_HEIGHT;
        int width = Math.max(dp(40), Math.round(floatValue(node.config, "scene_width", 280f) * scaleX));
        int height = Math.max(dp(32), Math.round(floatValue(node.config, "scene_height", 72f) * scaleY));
        ViewGroup.LayoutParams params = target.getLayoutParams();
        params.width = width;
        params.height = height;
        target.setLayoutParams(params);
        target.setX(floatValue(node.config, "scene_x", 40f) * scaleX);
        target.setY(floatValue(node.config, "scene_y", 284f) * scaleY);
    }

    private float floatValue(Map<String, Object> config, String key, float fallback) {
        if (config == null || config.get(key) == null) return fallback;
        try {
            Object value = config.get(key);
            return value instanceof Number ? ((Number) value).floatValue()
                    : Float.parseFloat(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void renderVideo(Map<String, Object> config) {
        String assetUrl = stringValue(config, "asset_url", "");
        if (assetUrl.isEmpty()) {
            recordCurrentError("missing_video_url");
            Toast.makeText(this, "Video URL is missing; step skipped", Toast.LENGTH_LONG).show();
            previewRoot.post(this::goNext);
            return;
        }
        videoPreview.setVisibility(View.VISIBLE);
        videoPreview.setVideoURI(Uri.parse(RetrofitClient.resolveMediaUrl(assetUrl)));
        videoPreview.setOnCompletionListener(player -> goNext());
        videoPreview.setOnErrorListener((player, what, extra) -> {
            recordCurrentError("video_playback_error_" + what + "_" + extra);
            Toast.makeText(this, "Cannot play selected video; step skipped", Toast.LENGTH_LONG).show();
            previewRoot.post(this::goNext);
            return true;
        });
        videoPreview.start();
    }

    private void startBackgroundMusic() {
        String soundUrl = QuestPreviewStore.backgroundSoundUrl;
        if (runFinished || backgroundMusicPlayer != null
                || soundUrl == null || soundUrl.trim().isEmpty()) return;
        MediaPlayer player = new MediaPlayer();
        backgroundMusicPlayer = player;
        try {
            float volume = Math.max(0f, Math.min(1f,
                    QuestPreviewStore.backgroundSoundVolume / 100f));
            player.setDataSource(RetrofitClient.resolveMediaUrl(soundUrl));
            player.setLooping(true);
            player.setVolume(volume, volume);
            player.setOnPreparedListener(prepared -> {
                if (backgroundMusicPlayer == prepared && !runFinished) prepared.start();
            });
            player.setOnErrorListener((failed, what, extra) -> {
                if (backgroundMusicPlayer == failed) {
                    releaseBackgroundMusic();
                    Toast.makeText(this, "Cannot play background music", Toast.LENGTH_LONG).show();
                }
                return true;
            });
            player.prepareAsync();
        } catch (Exception error) {
            releaseBackgroundMusic();
            Toast.makeText(this, "Cannot load background music", Toast.LENGTH_LONG).show();
        }
    }

    private void releaseBackgroundMusic() {
        if (backgroundMusicPlayer == null) return;
        try {
            backgroundMusicPlayer.stop();
        } catch (RuntimeException ignored) {
        }
        backgroundMusicPlayer.release();
        backgroundMusicPlayer = null;
    }

    private void pauseBackgroundMusicForVoice() {
        if (backgroundMusicPlayer == null) return;
        try {
            if (backgroundMusicPlayer.isPlaying()) backgroundMusicPlayer.pause();
        } catch (RuntimeException ignored) {
        }
    }

    private void resumeBackgroundMusicAfterVoice() {
        if (backgroundMusicPlayer == null || runFinished) return;
        try {
            backgroundMusicPlayer.start();
        } catch (RuntimeException ignored) {
        }
    }

    private void renderAudio(Map<String, Object> config) {
        String assetUrl = stringValue(config, "asset_url", "");
        if (assetUrl.isEmpty()) {
            recordCurrentError("missing_audio_url");
            Toast.makeText(this, "Audio URL is missing; step skipped", Toast.LENGTH_LONG).show();
            previewRoot.post(this::goNext);
            return;
        }
        try {
            audioPlayer = MediaPlayer.create(this, Uri.parse(RetrofitClient.resolveMediaUrl(assetUrl)));
            if (audioPlayer == null) throw new IllegalStateException("Unsupported audio file");
            float volume = Math.max(0f, Math.min(1f, intValue(config, "volume", 80) / 100f));
            audioPlayer.setVolume(volume, volume);
            audioPlayer.setOnCompletionListener(player -> goNext());
            audioPlayer.start();
        } catch (Exception error) {
            recordCurrentError("audio_playback_error");
            Toast.makeText(this, "Cannot play selected audio; step skipped", Toast.LENGTH_LONG).show();
            previewRoot.post(this::goNext);
        }
    }

    private void renderParallelGroup(QuestDraftRequest.QuestFlowNode parallelNode) {
        List<QuestDraftRequest.QuestFlowNode> branches = parallelChildren(parallelNode.client_node_id);
        if (branches.size() != 2) {
            Toast.makeText(this, "Parallel needs exactly two outgoing engine links", Toast.LENGTH_LONG).show();
            return;
        }

        previewContent.setVisibility(View.GONE);
        parallelLayer.setVisibility(View.VISIBLE);
        boolean[] completed = new boolean[]{false, false};
        boolean[] ready = new boolean[]{false, false};
        boolean[] groupFinished = new boolean[]{false};
        String condition = stringValue(parallelNode.config, "completion_condition", "A_OR_B");

        Runnable evaluate = () -> {
            if (!ready[0] || !ready[1] || groupFinished[0]) return;
            boolean done;
            switch (condition) {
                case "A": done = completed[0]; break;
                case "B": done = completed[1]; break;
                case "A_AND_B": done = completed[0] && completed[1]; break;
                default: done = completed[0] || completed[1]; break;
            }
            if (done) {
                groupFinished[0] = true;
                nextIndexOverride = parallelResumeIndex(branches.get(0), branches.get(1));
                goNext();
            }
        };

        renderParallelBranch(branches.get(0), 0, () -> {
            completed[0] = true;
            evaluate.run();
        });
        renderParallelBranch(branches.get(1), 1, () -> {
            completed[1] = true;
            evaluate.run();
        });
        ready[0] = true;
        ready[1] = true;
        evaluate.run();
    }

    private void renderParallelLink(QuestDraftRequest.QuestFlowEdge edge) {
        QuestDraftRequest.QuestFlowNode branchA = nodeMap.get(edge.source_client_node_id);
        QuestDraftRequest.QuestFlowNode branchB = nodeMap.get(edge.target_client_node_id);
        if (branchA == null || branchB == null) {
            Toast.makeText(this, "Parallel link is missing one engine", Toast.LENGTH_LONG).show();
            return;
        }

        previewContent.setVisibility(View.GONE);
        parallelLayer.setVisibility(View.VISIBLE);
        boolean[] completed = new boolean[]{false, false};
        boolean[] ready = new boolean[]{false, false};
        boolean[] groupFinished = new boolean[]{false};
        String condition = edge.completion_condition != null
                ? edge.completion_condition
                : stringValue(edge.config, "completion_condition", "A_OR_B");

        Runnable evaluate = () -> {
            if (!ready[0] || !ready[1] || groupFinished[0]) return;
            boolean done;
            switch (condition) {
                case "A": done = completed[0]; break;
                case "B": done = completed[1]; break;
                case "A_AND_B": done = completed[0] && completed[1]; break;
                default: done = completed[0] || completed[1]; break;
            }
            if (done) {
                groupFinished[0] = true;
                nextIndexOverride = parallelResumeIndex(branchA, branchB);
                goNext();
            }
        };

        renderParallelBranch(branchA, 0, () -> {
            completed[0] = true;
            evaluate.run();
        });
        renderParallelBranch(branchB, 1, () -> {
            completed[1] = true;
            evaluate.run();
        });
        ready[0] = true;
        ready[1] = true;
        evaluate.run();
    }

    private List<QuestDraftRequest.QuestFlowNode> parallelChildren(String parallelNodeId) {
        if (QuestPreviewStore.edges == null) return Collections.emptyList();
        List<QuestDraftRequest.QuestFlowEdge> branchEdges = new ArrayList<>();
        for (QuestDraftRequest.QuestFlowEdge edge : QuestPreviewStore.edges) {
            if (parallelNodeId.equals(edge.source_client_node_id)) {
                branchEdges.add(edge);
            }
        }
        branchEdges.sort((first, second) -> Integer.compare(
                parallelBranchOrder(first), parallelBranchOrder(second)));
        List<QuestDraftRequest.QuestFlowNode> children = new ArrayList<>();
        for (QuestDraftRequest.QuestFlowEdge edge : branchEdges) {
            QuestDraftRequest.QuestFlowNode child = nodeMap.get(edge.target_client_node_id);
            if (child != null) children.add(child);
        }
        return children;
    }

    private int parallelBranchOrder(QuestDraftRequest.QuestFlowEdge edge) {
        if (edge.config != null && edge.config.get("parallel_branch") != null) {
            return "B".equals(String.valueOf(edge.config.get("parallel_branch"))) ? 1 : 0;
        }
        return Math.max(0, edge.sort_order);
    }

    private int parallelResumeIndex(QuestDraftRequest.QuestFlowNode branchA,
                                    QuestDraftRequest.QuestFlowNode branchB) {
        String nextA = firstSequentialOutgoingTarget(branchA.client_node_id);
        String nextB = firstSequentialOutgoingTarget(branchB.client_node_id);
        if (nextA == null && nextB == null) return orderedNodes.size();
        if (nextA == null || nextB == null || !nextA.equals(nextB)) {
            return indexForNodeId(nextA != null ? nextA : nextB);
        }
        return indexForNodeId(nextA);
    }

    private int indexForNodeId(String nodeId) {
        if (nodeId == null) return orderedNodes.size();
        for (int i = 0; i < orderedNodes.size(); i++) {
            if (nodeId.equals(orderedNodes.get(i).client_node_id)) return i;
        }
        QuestDraftRequest.QuestFlowNode next = nodeMap.get(nodeId);
        if (next != null) {
            orderedNodes.add(next);
            return orderedNodes.size() - 1;
        }
        return orderedNodes.size();
    }

    private String firstSequentialOutgoingTarget(String nodeId) {
        if (QuestPreviewStore.edges == null) return null;
        for (QuestDraftRequest.QuestFlowEdge edge : QuestPreviewStore.edges) {
            if ("sequential".equals(edge.flow_type) && nodeId.equals(edge.source_client_node_id)) {
                return edge.target_client_node_id;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void renderParallelBranch(QuestDraftRequest.QuestFlowNode node, int branchIndex,
                                      Runnable onComplete) {
        Map<String, Object> config = node.config;
        recordEvent(node.client_node_id, "node_started");
        boolean[] eventCompleted = new boolean[]{false};
        Runnable completeBranch = () -> {
            if (eventCompleted[0]) return;
            eventCompleted[0] = true;
            recordEvent(node.client_node_id, "node_completed");
            onComplete.run();
        };
        switch (node.engine_subtype) {
            case "text": {
                TextView textView = new TextView(this);
                textView.setText(stringValue(config, "text", node.display_name));
                textView.setTextSize(intValue(config, "size", 22));
                textView.setTextColor(Color.parseColor("#111827"));
                textView.setGravity(Gravity.CENTER);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
                params.topMargin = branchIndex == 0 ? -dp(70) : dp(70);
                parallelLayer.addView(textView, params);
                if (!renderParallelAttached(config, textView, completeBranch)) completeBranch.run();
                break;
            }
            case "image": {
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int width = dp(intValue(config, "width", 240));
                int height = dp(intValue(config, "height", 180));
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height,
                        gravityForPosition(stringValue(config, "position", "center")));
                parallelLayer.addView(imageView, params);
                Glide.with(this).load(RetrofitClient.resolveMediaUrl(
                        stringValue(config, "asset_url", ""))).into(imageView);
                if (!renderParallelAttached(config, imageView, completeBranch)) completeBranch.run();
                break;
            }
            case "timer": {
                TextView timerText = new TextView(this);
                timerText.setTextSize(intValue(config, "size", 48));
                timerText.setTextColor(Color.parseColor("#111827"));
                parallelLayer.addView(timerText, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                startParallelTimer(intValue(config, "duration_seconds", 5), timerText, completeBranch);
                break;
            }
            case "video": {
                VideoView video = new VideoView(this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(420), dp(260), Gravity.CENTER);
                parallelLayer.addView(video, params);
                String assetUrl = stringValue(config, "asset_url", "");
                if (assetUrl.isEmpty()) {
                    recordError(node.client_node_id, "missing_parallel_video_url");
                    Toast.makeText(this, "Parallel video URL is missing; branch skipped",
                            Toast.LENGTH_LONG).show();
                    completeBranch.run();
                    break;
                }
                video.setVideoURI(Uri.parse(RetrofitClient.resolveMediaUrl(assetUrl)));
                video.setOnCompletionListener(player -> completeBranch.run());
                video.setOnErrorListener((player, what, extra) -> {
                    recordError(node.client_node_id, "parallel_video_playback_error_" + what + "_" + extra);
                    Toast.makeText(this, "Parallel video failed; branch skipped",
                            Toast.LENGTH_LONG).show();
                    completeBranch.run();
                    return true;
                });
                parallelVideos.add(video);
                video.start();
                break;
            }
            case "audio":
                startParallelAudio(config, completeBranch);
                break;
            case "gesture": {
                Button gestureTarget = new Button(this);
                String gestureType = stringValue(config, "gesture_type", "tap");
                String targetLabel = stringValue(config, "action_label", "").trim();
                if (targetLabel.isEmpty()) {
                    targetLabel = node.display_name == null || node.display_name.trim().isEmpty()
                            ? gestureType : node.display_name;
                }
                gestureTarget.setText(targetLabel);
                gestureTarget.setAllCaps(false);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        dp(220), dp(56), Gravity.CENTER);
                params.topMargin = branchIndex == 0 ? -dp(70) : dp(70);
                parallelLayer.addView(gestureTarget, params);
                configureGestureCallback(gestureType, config, gestureTarget, completeBranch);
                break;
            }
            case "sensor":
                startSensor(config, completeBranch);
                break;
            case "voice":
                startVoice(config, completeBranch);
                break;
            default:
                recordError(node.client_node_id, "unsupported_parallel_branch");
                Toast.makeText(this, "Unsupported parallel branch: " + node.engine_subtype,
                        Toast.LENGTH_SHORT).show();
                completeBranch.run();
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean renderParallelAttached(Map<String, Object> config, View outputView,
                                           Runnable onComplete) {
        if (config == null || !(config.get("attached_engine") instanceof Map)) return false;
        Map<String, Object> attached = (Map<String, Object>) config.get("attached_engine");
        String type = stringValue(attached, "engine_subtype", "");
        if ("timer".equals(type)) {
            startParallelTimer(intValue(attached, "duration_seconds", 1), null, onComplete);
        } else if ("gesture".equals(type)) {
            configureGestureCallback(stringValue(attached, "gesture_type", "tap"),
                    attached, outputView, onComplete);
        } else if ("sensor".equals(type)) {
            startSensor(attached, onComplete);
        } else if ("voice".equals(type)) {
            startVoice(attached, onComplete);
        } else {
            return false;
        }
        return true;
    }

    private void startParallelTimer(int seconds, TextView timerText, Runnable onComplete) {
        CountDownTimer branchTimer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timerText != null) timerText.setText(String.valueOf((millisUntilFinished / 1000L) + 1));
            }

            @Override
            public void onFinish() {
                onComplete.run();
            }
        };
        parallelTimers.add(branchTimer);
        branchTimer.start();
    }

    private void startParallelAudio(Map<String, Object> config, Runnable onComplete) {
        MediaPlayer player = MediaPlayer.create(this, Uri.parse(RetrofitClient.resolveMediaUrl(
                stringValue(config, "asset_url", ""))));
        if (player == null) {
            onComplete.run();
            return;
        }
        float volume = Math.max(0f, Math.min(1f, intValue(config, "volume", 80) / 100f));
        player.setVolume(volume, volume);
        player.setOnCompletionListener(mediaPlayer -> onComplete.run());
        parallelPlayers.add(player);
        player.start();
    }

    private void configureGestureCallback(String type, Map<String, Object> config, View target,
                                          Runnable onComplete) {
        final float[] start = new float[2];
        final long[] startTime = new long[1];
        final int[] count = new int[1];
        final boolean[] done = new boolean[1];
        target.setClickable(true);
        target.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                start[0] = event.getX();
                start[1] = event.getY();
                startTime[0] = SystemClock.elapsedRealtime();
                if ("hold".equals(type)) animateHoldStart(target);
                return true;
            }
            float dx = event.getX() - start[0];
            float dy = event.getY() - start[1];
            if ("swipe".equals(type) && event.getAction() == MotionEvent.ACTION_MOVE
                    && !done[0] && matchesSwipe(config, dx, dy)) {
                done[0] = true;
                animateSwipeOut(target, stringValue(config, "direction", "right"), onComplete);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                animateHoldEnd(target);
                return true;
            }
            if (event.getAction() != MotionEvent.ACTION_UP || done[0]) return true;
            if ("tap".equals(type)) {
                done[0] = true;
                animateTapFeedback(target);
            } else if ("spam_tap".equals(type)) {
                count[0]++;
                animateTapFeedback(target);
                done[0] = count[0] >= intValue(config, "required_count", 2);
            } else if ("hold".equals(type)) {
                animateHoldEnd(target);
                done[0] = SystemClock.elapsedRealtime() - startTime[0]
                        >= intValue(config, "duration_seconds", 1) * 1000L;
            }
            if (done[0]) target.postDelayed(onComplete, "hold".equals(type) ? 160L : 220L);
            return true;
        });
    }

    private int gravityForPosition(String position) {
        switch (position) {
            case "top": return Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            case "bottom": return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            case "left": return Gravity.START | Gravity.CENTER_VERTICAL;
            case "right": return Gravity.END | Gravity.CENTER_VERTICAL;
            default: return Gravity.CENTER;
        }
    }

    private void stopParallelResources() {
        for (CountDownTimer branchTimer : parallelTimers) branchTimer.cancel();
        parallelTimers.clear();
        for (MediaPlayer player : parallelPlayers) {
            try {
                player.release();
            } catch (Exception ignored) {
            }
        }
        parallelPlayers.clear();
        for (VideoView video : parallelVideos) video.stopPlayback();
        parallelVideos.clear();
        stopVoiceCapture(false);
        if (sensorManager != null) {
            for (SensorEventListener listener : new ArrayList<>(activeSensorListeners)) {
                sensorManager.unregisterListener(listener);
            }
        }
        activeSensorListeners.clear();
    }

    private void applyImagePosition(String position) {
        imgPreview.post(() -> {
            float horizontalOffset = previewRoot.getWidth() * 0.28f;
            float verticalOffset = previewRoot.getHeight() * 0.25f;
            imgPreview.setTranslationX(0f);
            imgPreview.setTranslationY(0f);
            switch (position) {
                case "left": imgPreview.setTranslationX(-horizontalOffset); break;
                case "right": imgPreview.setTranslationX(horizontalOffset); break;
                case "top": imgPreview.setTranslationY(-verticalOffset); break;
                case "bottom": imgPreview.setTranslationY(verticalOffset); break;
                default: break;
            }
        });
    }

    private void renderGesture(Map<String, Object> config) {
        String type = stringValue(config, "gesture_type", "tap");
        configureGesture(type, config, previewRoot, false);
    }

    private void startTimer(int seconds, int numberSize, boolean attachedToOutput) {
        TextView timerView = attachedToOutput ? null : tvMain;
        if (timerView != null) {
            timerView.setVisibility(View.VISIBLE);
            timerView.setTextSize(numberSize);
        }
        timer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (timerView != null) {
                    timerView.setText(String.valueOf((millisUntilFinished / 1000L) + 1));
                }
            }

            @Override
            public void onFinish() {
                goNext();
            }
        };
        timer.start();
    }

    @SuppressWarnings("unchecked")
    private boolean renderAttachedEngine(Map<String, Object> config, View outputView) {
        if (config == null || !(config.get("attached_engine") instanceof Map)) return false;
        Map<String, Object> attached = (Map<String, Object>) config.get("attached_engine");
        String type = stringValue(attached, "engine_subtype", "");
        if ("timer".equals(type)) {
            startTimer(intValue(attached, "duration_seconds", 1), 0, true);
        } else if ("gesture".equals(type)) {
            configureGesture(stringValue(attached, "gesture_type", "tap"), attached, outputView, true);
        } else if ("text_input".equals(type)) {
            configureTextInput(attached, this::goNext);
        } else if ("sensor".equals(type)) {
            startSensor(attached, this::goNext);
        } else if ("voice".equals(type)) {
            startVoice(attached, this::goNext);
        } else {
            return false;
        }
        return true;
    }

    private void configureTextInput(Map<String, Object> config, Runnable onSubmit) {
        int wordLimit = Math.max(1, Math.min(1000, intValue(config, "word_limit", 60)));
        etPreviewInput.setVisibility(View.VISIBLE);
        etPreviewInput.setHint(stringValue(config, "placeholder", ""));
        etPreviewInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        tvHint.setVisibility(View.VISIBLE);
        tvHint.setText("0/" + wordLimit + " words");

        inputWordLimitWatcher = new TextWatcher() {
            private boolean updating;

            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) { }

            @Override public void afterTextChanged(Editable editable) {
                if (updating) return;
                int count = countWords(editable.toString());
                if (count > wordLimit) {
                    updating = true;
                    String limited = firstWords(editable.toString(), wordLimit);
                    editable.replace(0, editable.length(), limited);
                    updating = false;
                    count = wordLimit;
                    Toast.makeText(QuestPreviewActivity.this,
                            "Word limit reached: " + wordLimit, Toast.LENGTH_SHORT).show();
                }
                tvHint.setText(count + "/" + wordLimit + " words");
            }
        };
        etPreviewInput.addTextChangedListener(inputWordLimitWatcher);
        etPreviewInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) return false;
            int wordCount = countWords(view.getText().toString());
            if (wordCount == 0) {
                Toast.makeText(this, "Please enter a response", Toast.LENGTH_SHORT).show();
                return true;
            }
            recordInputForCurrentNode(wordCount);
            onSubmit.run();
            return true;
        });
    }

    private int countWords(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
    }

    private String firstWords(String value, int limit) {
        String[] words = value.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < Math.min(limit, words.length); index++) {
            if (index > 0) result.append(' ');
            result.append(words[index]);
        }
        return result.toString();
    }

    private void startVoice(Map<String, Object> config, Runnable onComplete) {
        if (voiceRecorder != null || pendingVoiceCompletion != null) {
            Toast.makeText(this, "Only one voice input can run at a time; extra input skipped",
                    Toast.LENGTH_LONG).show();
            onComplete.run();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            pendingVoiceConfig = config;
            pendingVoiceCompletion = onComplete;
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            return;
        }
        beginVoiceCapture(config, onComplete);
    }

    @SuppressWarnings("deprecation")
    private void beginVoiceCapture(Map<String, Object> config, Runnable onComplete) {
        int durationSeconds = Math.max(1, Math.min(60,
                intValue(config, "duration_seconds", 5)));
        String mode = stringValue(config, "mode", "breath");
        try {
            voiceTempFile = new File(getCacheDir(), "quest_voice_" + System.currentTimeMillis() + ".3gp");
            voiceRecorder = new MediaRecorder();
            voiceRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            voiceRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            voiceRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            voiceRecorder.setOutputFile(voiceTempFile.getAbsolutePath());
            voiceRecorder.prepare();
            voiceRecorder.start();
            pauseBackgroundMusicForVoice();
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText("Listening (" + mode + ") for " + durationSeconds + " seconds");

            voicePollRunnable = new Runnable() {
                @Override public void run() {
                    if (voiceRecorder == null) return;
                    try {
                        int amplitude = voiceRecorder.getMaxAmplitude();
                        tvHint.setText("Listening (" + mode + ") • level " + amplitude);
                    } catch (RuntimeException ignored) {
                    }
                    transitionHandler.postDelayed(this, 250L);
                }
            };
            voiceStopRunnable = () -> stopVoiceCapture(true);
            pendingVoiceCompletion = onComplete;
            transitionHandler.post(voicePollRunnable);
            transitionHandler.postDelayed(voiceStopRunnable, durationSeconds * 1000L);
        } catch (Exception error) {
            stopVoiceCapture(false);
            Toast.makeText(this, "Cannot start microphone; voice step skipped",
                    Toast.LENGTH_LONG).show();
            onComplete.run();
        }
    }

    private void stopVoiceCapture(boolean complete) {
        if (voicePollRunnable != null) transitionHandler.removeCallbacks(voicePollRunnable);
        if (voiceStopRunnable != null) transitionHandler.removeCallbacks(voiceStopRunnable);
        voicePollRunnable = null;
        voiceStopRunnable = null;
        if (voiceRecorder != null) {
            try {
                voiceRecorder.stop();
            } catch (RuntimeException ignored) {
            }
            voiceRecorder.release();
            voiceRecorder = null;
        }
        if (voiceTempFile != null) {
            // Voice is used only as a completion interaction; do not retain sensitive audio.
            if (voiceTempFile.exists()) voiceTempFile.delete();
            voiceTempFile = null;
        }
        resumeBackgroundMusicAfterVoice();
        Runnable completion = pendingVoiceCompletion;
        pendingVoiceCompletion = null;
        if (complete && completion != null) completion.run();
    }

    private void startSensor(Map<String, Object> config, Runnable onComplete) {
        Sensor accelerometer = sensorManager == null ? null
                : sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Toast.makeText(this, "Accelerometer unavailable; sensor step skipped",
                    Toast.LENGTH_LONG).show();
            onComplete.run();
            return;
        }
        String action = stringValue(config, "sensor_action", "face_down");
        int timeoutSeconds = Math.max(1, Math.min(60,
                intValue(config, "duration_seconds", 5)));
        boolean[] done = new boolean[]{false};
        int[] matchingSamples = new int[]{0};
        SensorEventListener[] holder = new SensorEventListener[1];
        Runnable finish = () -> {
            if (done[0]) return;
            done[0] = true;
            sensorManager.unregisterListener(holder[0]);
            activeSensorListeners.remove(holder[0]);
            onComplete.run();
        };
        holder[0] = new SensorEventListener() {
            @Override public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                double magnitude = Math.sqrt(x * x + y * y + z * z);
                boolean matches;
                switch (action) {
                    case "shake": matches = magnitude > 18.0; break;
                    case "tilt": matches = Math.sqrt(x * x + y * y) > 6.5; break;
                    default: matches = z < -7.0; break;
                }
                matchingSamples[0] = matches ? matchingSamples[0] + 1 : 0;
                if (matchingSamples[0] >= 2) finish.run();
            }

            @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        activeSensorListeners.add(holder[0]);
        tvHint.setVisibility(View.VISIBLE);
        tvHint.setText("Waiting for phone action: " + action);
        if (!sensorManager.registerListener(holder[0], accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
            activeSensorListeners.remove(holder[0]);
            Toast.makeText(this, "Cannot start accelerometer; sensor step skipped",
                    Toast.LENGTH_LONG).show();
            onComplete.run();
            return;
        }
        transitionHandler.postDelayed(() -> {
            if (done[0]) return;
            Toast.makeText(this, "Sensor timed out; step skipped", Toast.LENGTH_LONG).show();
            finish.run();
        }, timeoutSeconds * 1000L);
    }

    private void advancePastPassiveOutput() {
        if (currentIndex < orderedNodes.size() - 1) {
            previewRoot.post(this::goNext);
        }
    }

    private void configureGesture(String type, Map<String, Object> config,
                                  View interactionTarget, boolean attachedToOutput) {
        tapCount = 0;
        gestureCompleted = false;
        String actionLabel = stringValue(config, "action_label", "").trim();
        if (!attachedToOutput && "tap".equals(type) && !actionLabel.isEmpty()) {
            btnCompleteNode.setText(actionLabel);
            btnCompleteNode.setVisibility(View.VISIBLE);
            btnCompleteNode.setOnClickListener(view -> {
                animateTapFeedback(view);
                view.postDelayed(this::goNext, 220L);
            });
            return;
        }
        interactionTarget.setClickable(true);
        interactionTarget.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchStartX = event.getX();
                touchStartY = event.getY();
                touchStartTime = SystemClock.elapsedRealtime();
                if ("hold".equals(type)) animateHoldStart(interactionTarget);
                return true;
            }

            float dx = event.getX() - touchStartX;
            float dy = event.getY() - touchStartY;
            long elapsed = SystemClock.elapsedRealtime() - touchStartTime;

            if ("swipe".equals(type) && event.getAction() == MotionEvent.ACTION_MOVE
                    && !gestureCompleted && matchesSwipe(config, dx, dy)) {
                gestureCompleted = true;
                animateSwipeOut(interactionTarget,
                        stringValue(config, "direction", "right"), this::goNext);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                animateHoldEnd(interactionTarget);
                return true;
            }
            if (event.getAction() != MotionEvent.ACTION_UP || gestureCompleted) return true;

            if ("tap".equals(type)) {
                animateTapFeedback(interactionTarget);
                interactionTarget.postDelayed(this::goNext, 220L);
            } else if ("spam_tap".equals(type)) {
                tapCount++;
                animateTapFeedback(interactionTarget);
                if (tapCount >= intValue(config, "required_count", 2)) {
                    interactionTarget.postDelayed(this::goNext, 220L);
                }
            } else if ("hold".equals(type)) {
                animateHoldEnd(interactionTarget);
                if (elapsed >= intValue(config, "duration_seconds", 1) * 1000L) {
                    interactionTarget.postDelayed(this::goNext, 160L);
                }
            } else if ("swipe".equals(type) && matchesSwipe(config, dx, dy)) {
                gestureCompleted = true;
                animateSwipeOut(interactionTarget,
                        stringValue(config, "direction", "right"), this::goNext);
            }
            return true;
        });
    }

    private boolean matchesSwipe(Map<String, Object> config, float dx, float dy) {
        String direction = stringValue(config, "direction", "right").trim().toLowerCase();
        float threshold = dp(32);
        switch (direction) {
            case "left": return dx < -threshold && Math.abs(dx) > Math.abs(dy);
            case "up": return dy < -threshold && Math.abs(dy) > Math.abs(dx);
            case "down": return dy > threshold && Math.abs(dy) > Math.abs(dx);
            default: return dx > threshold && Math.abs(dx) > Math.abs(dy);
        }
    }

    private void animateTapFeedback(View target) {
        target.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        float baseX = target.getTranslationX();
        ObjectAnimator shake = ObjectAnimator.ofFloat(target, View.TRANSLATION_X,
                baseX, baseX - dp(8), baseX + dp(8), baseX - dp(5), baseX + dp(5), baseX);
        shake.setDuration(200L);
        shake.start();
    }

    private void animateHoldStart(View target) {
        target.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        target.animate().scaleX(0.94f).scaleY(0.94f).setDuration(120L).start();
    }

    private void animateHoldEnd(View target) {
        target.animate().scaleX(1f).scaleY(1f).setDuration(140L).start();
    }

    private void animateSwipeOut(View target, String rawDirection, Runnable onComplete) {
        String direction = rawDirection == null ? "right" : rawDirection.trim().toLowerCase();
        float endX = target.getTranslationX();
        float endY = target.getTranslationY();
        float horizontalDistance = Math.max(previewRoot.getWidth(), dp(480));
        float verticalDistance = Math.max(previewRoot.getHeight(), dp(320));
        switch (direction) {
            case "left": endX -= horizontalDistance; break;
            case "up": endY -= verticalDistance; break;
            case "down": endY += verticalDistance; break;
            default: endX += horizontalDistance; break;
        }
        target.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        target.animate()
                .translationX(endX)
                .translationY(endY)
                .alpha(0f)
                .setDuration(280L)
                .withEndAction(onComplete)
                .start();
    }

    private void resetGestureEffect(View target) {
        target.animate().cancel();
        target.setAlpha(1f);
        target.setScaleX(1f);
        target.setScaleY(1f);
        target.setTranslationX(0f);
        target.setTranslationY(0f);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void goNext() {
        if (transitionScheduled) return;
        if (currentIndex >= 0 && currentIndex < orderedNodes.size()) {
            String completedNodeId = orderedNodes.get(currentIndex).client_node_id;
            recordEvent(completedNodeId, "node_completed");
            List<String> completedFrames = frameCompletionsByTerminal.remove(completedNodeId);
            if (completedFrames != null) {
                for (String frameId : completedFrames) recordEvent(frameId, "node_completed");
            }
        }
        if (currentIndex >= orderedNodes.size() - 1) {
            currentIndex++;
            renderCurrentNode();
            return;
        }

        transitionScheduled = true;
        previewRoot.setOnTouchListener(null);
        etPreviewInput.setEnabled(false);
        QuestDraftRequest.QuestFlowEdge edge = currentSequentialEdge();
        String transitionType = edge == null
                ? "delay" : stringValue(edge.config, "transition_type", "delay");
        long delayMillis = "immediate".equals(transitionType)
                ? 0L : currentEdgeDelaySeconds() * 1000L;
        String effect = edge == null
                ? "fade" : stringValue(edge.config, "transition_effect", "fade");
        transitionHandler.postDelayed(() -> playTransitionEffect(effect), delayMillis);
    }

    private void playTransitionEffect(String rawEffect) {
        String effect = rawEffect == null ? "none" : rawEffect;
        Runnable showNext = () -> {
            currentIndex = nextIndexOverride == null ? currentIndex + 1 : nextIndexOverride;
            nextIndexOverride = null;
            renderCurrentNode();
            if ("fade".equals(effect)) {
                previewContent.setAlpha(0f);
                previewContent.animate().alpha(1f).setDuration(260L).start();
            } else if ("slide_left".equals(effect) || "slide_right".equals(effect)) {
                float start = "slide_left".equals(effect) ? dp(96) : -dp(96);
                previewContent.setTranslationX(start);
                previewContent.setAlpha(0f);
                previewContent.animate().translationX(0f).alpha(1f).setDuration(300L).start();
            } else if ("zoom".equals(effect)) {
                previewContent.setScaleX(0.88f);
                previewContent.setScaleY(0.88f);
                previewContent.setAlpha(0f);
                previewContent.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(280L).start();
            }
        };

        if ("none".equals(effect)) {
            showNext.run();
        } else if ("slide_left".equals(effect) || "slide_right".equals(effect)) {
            float target = "slide_left".equals(effect) ? -dp(96) : dp(96);
            previewContent.animate().translationX(target).alpha(0f).setDuration(220L)
                    .withEndAction(showNext).start();
        } else if ("zoom".equals(effect)) {
            previewContent.animate().scaleX(1.08f).scaleY(1.08f).alpha(0f).setDuration(200L)
                    .withEndAction(showNext).start();
        } else {
            previewContent.animate().alpha(0f).setDuration(220L)
                    .withEndAction(showNext).start();
        }
    }

    private void resetTransitionEffect() {
        if (previewContent == null) return;
        previewContent.animate().cancel();
        previewContent.setAlpha(1f);
        previewContent.setTranslationX(0f);
        previewContent.setTranslationY(0f);
        previewContent.setScaleX(1f);
        previewContent.setScaleY(1f);
    }

    private void recordEvent(String nodeId, String eventType) {
        if ("node_completed".equals(eventType) && nodeId != null) completedNodeIds.add(nodeId);
        if (runId > 0 && !runFinished) runRepository.appendRunEvent(runToken, runId, nodeId, eventType);
    }

    private Map<String, Object> buildRunSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("client_duration_seconds",
                Math.max(0L, (System.currentTimeMillis() - runStartedAtMs) / 1000L));
        summary.put("completed_node_count", completedNodeIds.size());
        if (currentIndex >= 0 && currentIndex < orderedNodes.size()) {
            summary.put("last_node_id", orderedNodes.get(currentIndex).client_node_id);
        }
        return summary;
    }

    private void recordCurrentError(String reason) {
        if (currentIndex < 0 || currentIndex >= orderedNodes.size()) return;
        recordError(orderedNodes.get(currentIndex).client_node_id, reason);
    }

    private void recordError(String nodeId, String reason) {
        if (runId <= 0 || runFinished) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("reason", reason);
        runRepository.appendRunEvent(runToken, runId, nodeId, "error", payload);
    }

    private void recordInputForCurrentNode(int wordCount) {
        if (runId <= 0 || runFinished || currentIndex < 0 || currentIndex >= orderedNodes.size()) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("word_count", wordCount);
        runRepository.appendRunEvent(runToken, runId,
                orderedNodes.get(currentIndex).client_node_id, "input_received", payload);
    }

    private void completeRun() {
        if (runId <= 0) {
            Toast.makeText(this, "Preview completed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (runFinished) return;
        runFinished = true;
        runRepository.finishQuestRun(runToken, runId, "completed", buildRunSummary(), new QuestBuilderRepository.RepositoryCallback<Object>() {
            @Override public void onSuccess(Object data, String message) {
                Toast.makeText(QuestPreviewActivity.this, "Quest completed", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onError(String message) {
                runFinished = false;
                Toast.makeText(QuestPreviewActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void closeRun() {
        if (runId <= 0 || runFinished) {
            finish();
            return;
        }
        runFinished = true;
        runRepository.finishQuestRun(runToken, runId, "abandoned", buildRunSummary(), new QuestBuilderRepository.RepositoryCallback<Object>() {
            @Override public void onSuccess(Object data, String message) { finish(); }
            @Override public void onError(String message) { finish(); }
        });
    }

    private int currentEdgeDelaySeconds() {
        QuestDraftRequest.QuestFlowEdge edge = currentSequentialEdge();
        return edge == null ? 3 : Math.max(0, intValue(edge.config, "delay_seconds", 3));
    }

    private QuestDraftRequest.QuestFlowEdge currentSequentialEdge() {
        if (QuestPreviewStore.edges == null || currentIndex >= orderedNodes.size() - 1) return null;
        String sourceId = orderedNodes.get(currentIndex).client_node_id;
        String targetId = orderedNodes.get(currentIndex + 1).client_node_id;
        for (QuestDraftRequest.QuestFlowEdge edge : QuestPreviewStore.edges) {
            if (sourceId.equals(edge.source_client_node_id)
                    && targetId.equals(edge.target_client_node_id)
                    && "sequential".equals(edge.flow_type)) return edge;
        }
        QuestDraftRequest.QuestFlowNode source = nodeMap.get(sourceId);
        QuestDraftRequest.QuestFlowNode target = nodeMap.get(targetId);
        String logicalSource = source != null && source.parent_client_node_id != null
                ? source.parent_client_node_id : sourceId;
        String logicalTarget = target != null && target.parent_client_node_id != null
                ? target.parent_client_node_id : targetId;
        for (QuestDraftRequest.QuestFlowEdge edge : QuestPreviewStore.edges) {
            if (logicalSource.equals(edge.source_client_node_id)
                    && logicalTarget.equals(edge.target_client_node_id)
                    && "sequential".equals(edge.flow_type)) return edge;
        }
        return null;
    }

    private void applyFrameBackground(QuestDraftRequest.QuestFlowNode node) {
        QuestDraftRequest.QuestFlowNode frame = node.parent_client_node_id == null
                ? null : nodeMap.get(node.parent_client_node_id);
        String frameUrl = frame == null ? "" : stringValue(frame.config, "background_url", "");
        String canvasUrl = QuestPreviewStore.backgroundUrl == null ? "" : QuestPreviewStore.backgroundUrl;
        String url = frameUrl.isEmpty() ? canvasUrl : frameUrl;
        if (url.isEmpty()) {
            imgBackground.setVisibility(View.GONE);
        } else {
            imgBackground.setVisibility(View.VISIBLE);
            Glide.with(this).load(RetrofitClient.resolveMediaUrl(url)).into(imgBackground);
        }
    }

    private String stringValue(Map<String, Object> config, String key, String fallback) {
        if (config == null || !config.containsKey(key) || config.get(key) == null) return fallback;
        String value = String.valueOf(config.get(key));
        return "null".equals(value) ? fallback : value;
    }

    private int intValue(Map<String, Object> config, String key, int fallback) {
        if (config == null || !config.containsKey(key) || config.get(key) == null) return fallback;
        try {
            Object value = config.get(key);
            if (value instanceof Number) return ((Number) value).intValue();
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
