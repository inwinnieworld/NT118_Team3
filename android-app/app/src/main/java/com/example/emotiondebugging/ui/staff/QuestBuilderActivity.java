package com.example.emotiondebugging.ui.staff;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.domain.QuestEngine;
import com.example.emotiondebugging.model.domain.QuestProblem;
import com.example.emotiondebugging.model.request.QuestDraftRequest;
import com.example.emotiondebugging.model.response.QuestDraftDetail;
import com.example.emotiondebugging.model.response.QuestDraftSummary;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestBuilderActivity extends AppCompatActivity implements QuestCanvasView.CanvasListener {

    public static final String EXTRA_VERSION_ID = "quest_builder_version_id";

    private QuestBuilderViewModel viewModel;
    private SharedPrefsHelper prefsHelper;

    private LinearLayout layoutEngineButtons;
    private View layoutConfigPanel;
    private QuestCanvasView questCanvas;
    private EditText etQuestTitle;
    private EditText etQuestDescription;
    private EditText etQuestLevel;
    private EditText etNodeName;
    private EditText etPrimaryConfig;
    private Button btnPickPrimaryAsset;
    private EditText etSecondaryConfig;
    private EditText etBackgroundSoundUrl;
    private EditText etBackgroundSoundVolume;
    private Button btnPickBackgroundSound;
    private Spinner spinnerGestureType;
    private Spinner spinnerAttachedEngine;
    private Spinner spinnerAttachedGestureType;
    private Spinner spinnerAttachedSwipeDirection;
    private Spinner spinnerMediaPosition;
    private Spinner spinnerSensorAction;
    private Spinner spinnerVoiceMode;
    private Spinner spinnerParallelCondition;
    private Spinner spinnerTransitionType;
    private Spinner spinnerTransitionEffect;
    private TextView tvTransitionTypeLabel;
    private TextView tvTransitionEffectLabel;
    private LinearLayout layoutImageSize;
    private EditText etImageWidth;
    private EditText etImageHeight;
    private EditText etAttachedPrimaryConfig;
    private EditText etAttachedSecondaryConfig;
    private TextView tvAttachedEngineTitle;
    private TextView tvConfigTitle;
    private Button btnSaveDraft;
    private Button btnSubmitReview;
    private Button btnApplyConfig;
    private Button btnRemoveNode;
    private Button btnConnectMode;
    private Button btnPreviewQuest;
    private Button btnAiMetadata;
    private Button btnOpenDraft;
    private Button btnDesignPosition;
    private Button btnOpenFrame;
    private boolean openDraftRequested;
    private boolean problemDialogRequested;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> backgroundSoundPickerLauncher;
    private final ActivityResultLauncher<Intent> sceneDesignerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                Intent data = result.getData();
                viewModel.updateSelectedSceneLayout(
                        data.getFloatExtra(QuestSceneDesignerActivity.EXTRA_SCENE_X, 0f),
                        data.getFloatExtra(QuestSceneDesignerActivity.EXTRA_SCENE_Y, 0f),
                        data.getFloatExtra(QuestSceneDesignerActivity.EXTRA_SCENE_WIDTH, 280f),
                        data.getFloatExtra(QuestSceneDesignerActivity.EXTRA_SCENE_HEIGHT, 72f)
                );
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_quest_builder);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        prefsHelper = new SharedPrefsHelper(this);
        viewModel = new ViewModelProvider(this).get(QuestBuilderViewModel.class);

        initViews();
        initObservers();
        initActions();
        viewModel.loadEngines(getAuthToken());
        viewModel.loadProblems(getAuthToken());
        int versionId = getIntent().getIntExtra(EXTRA_VERSION_ID, 0);
        if (versionId > 0) viewModel.openDraft(getAuthToken(), versionId);
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (!viewModel.exitCurrentFrame()) finish();
        });

        layoutEngineButtons = findViewById(R.id.layoutEngineButtons);
        layoutConfigPanel = findViewById(R.id.layoutConfigPanel);
        questCanvas = findViewById(R.id.questCanvas);
        questCanvas.setCanvasListener(this);

        etQuestTitle = findViewById(R.id.etQuestTitle);
        etQuestDescription = findViewById(R.id.etQuestDescription);
        etQuestLevel = findViewById(R.id.etQuestLevel);
        etNodeName = findViewById(R.id.etNodeName);
        etPrimaryConfig = findViewById(R.id.etPrimaryConfig);
        btnPickPrimaryAsset = findViewById(R.id.btnPickPrimaryAsset);
        etSecondaryConfig = findViewById(R.id.etSecondaryConfig);
        etBackgroundSoundUrl = findViewById(R.id.etBackgroundSoundUrl);
        etBackgroundSoundVolume = findViewById(R.id.etBackgroundSoundVolume);
        btnPickBackgroundSound = findViewById(R.id.btnPickBackgroundSound);
        spinnerGestureType = findViewById(R.id.spinnerGestureType);
        spinnerAttachedEngine = findViewById(R.id.spinnerAttachedEngine);
        spinnerAttachedGestureType = findViewById(R.id.spinnerAttachedGestureType);
        spinnerAttachedSwipeDirection = findViewById(R.id.spinnerAttachedSwipeDirection);
        spinnerMediaPosition = findViewById(R.id.spinnerMediaPosition);
        spinnerSensorAction = findViewById(R.id.spinnerSensorAction);
        spinnerVoiceMode = findViewById(R.id.spinnerVoiceMode);
        spinnerParallelCondition = findViewById(R.id.spinnerParallelCondition);
        spinnerTransitionType = findViewById(R.id.spinnerTransitionType);
        spinnerTransitionEffect = findViewById(R.id.spinnerTransitionEffect);
        tvTransitionTypeLabel = findViewById(R.id.tvTransitionTypeLabel);
        tvTransitionEffectLabel = findViewById(R.id.tvTransitionEffectLabel);
        layoutImageSize = findViewById(R.id.layoutImageSize);
        etImageWidth = findViewById(R.id.etImageWidth);
        etImageHeight = findViewById(R.id.etImageHeight);
        etAttachedPrimaryConfig = findViewById(R.id.etAttachedPrimaryConfig);
        etAttachedSecondaryConfig = findViewById(R.id.etAttachedSecondaryConfig);
        tvAttachedEngineTitle = findViewById(R.id.tvAttachedEngineTitle);
        tvConfigTitle = findViewById(R.id.tvConfigTitle);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnApplyConfig = findViewById(R.id.btnApplyConfig);
        btnRemoveNode = findViewById(R.id.btnRemoveNode);
        btnConnectMode = findViewById(R.id.btnConnectMode);
        btnPreviewQuest = findViewById(R.id.btnPreviewQuest);
        btnAiMetadata = findViewById(R.id.btnAiMetadata);
        btnOpenDraft = findViewById(R.id.btnOpenDraft);
        btnDesignPosition = findViewById(R.id.btnDesignPosition);
        btnOpenFrame = findViewById(R.id.btnOpenFrame);

        setupSpinners();
        setupImagePicker();
        setupCanvasDrop();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handlePickedImage
        );
        backgroundSoundPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::handlePickedBackgroundSound
        );
    }

    private void setupSpinners() {
        spinnerGestureType.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"tap", "spam_tap", "swipe", "hold"}
        ));
        spinnerGestureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGestureConfigHint(String.valueOf(parent.getItemAtPosition(position)), false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        dismissKeyboardBeforeSelection(spinnerGestureType);

        spinnerAttachedEngine.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"none", "timer", "gesture", "sensor", "voice", "text_input"}
        ));
        spinnerAttachedEngine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAttachedEngineFields(String.valueOf(parent.getItemAtPosition(position)), false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        dismissKeyboardBeforeSelection(spinnerAttachedEngine);

        spinnerAttachedGestureType.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"tap", "spam_tap", "swipe", "hold"}
        ));
        spinnerAttachedGestureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAttachedGestureFields(String.valueOf(parent.getItemAtPosition(position)), false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        dismissKeyboardBeforeSelection(spinnerAttachedGestureType);

        spinnerAttachedSwipeDirection.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"up", "down", "left", "right"}
        ));
        dismissKeyboardBeforeSelection(spinnerAttachedSwipeDirection);

        setupSelectionSpinner(spinnerMediaPosition,
                new String[]{"top", "bottom", "left", "right", "center"});
        setupSelectionSpinner(spinnerSensorAction,
                new String[]{"face_down", "shake", "tilt"});
        setupSelectionSpinner(spinnerVoiceMode,
                new String[]{"breath", "shout", "ambient"});
        setupSelectionSpinner(spinnerParallelCondition,
                new String[]{"A", "B", "A_OR_B", "A_AND_B"});
        setupSelectionSpinner(spinnerTransitionType,
                new String[]{"delay", "immediate"});
        setupSelectionSpinner(spinnerTransitionEffect,
                new String[]{"none", "fade", "slide_left", "slide_right", "zoom"});
        spinnerTransitionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean delay = "delay".equals(String.valueOf(parent.getItemAtPosition(position)));
                etPrimaryConfig.setVisibility(delay ? View.VISIBLE : View.GONE);
                if (delay) etPrimaryConfig.setHint("Delay before next engine (seconds)");
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupSelectionSpinner(Spinner spinner, String[] values) {
        spinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                values
        ));
        dismissKeyboardBeforeSelection(spinner);
    }

    private void dismissKeyboardBeforeSelection(Spinner spinner) {
        spinner.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                View focused = getCurrentFocus();
                if (focused != null && focused != spinner) {
                    InputMethodManager keyboard =
                            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    keyboard.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                    focused.clearFocus();
                }
                spinner.requestFocus();
            }
            return false;
        });
    }

    private void setupCanvasDrop() {
        questCanvas.setOnDragListener((view, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    Object local = event.getLocalState();
                    if (local instanceof QuestEngine) {
                        viewModel.addEngineAt((QuestEngine) local, event.getX(), event.getY());
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_STARTED:
                    ClipDescription description = event.getClipDescription();
                    return description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                default:
                    return true;
            }
        });
    }

    private void initObservers() {
        viewModel.getEngines().observe(this, this::renderEnginePanel);
        viewModel.getAiMetadataSummary().observe(this, summary ->
                btnAiMetadata.setText(summary == null ? "Chọn vấn đề" : summary));
        viewModel.getProblems().observe(this, problems -> {
            if (problemDialogRequested && problems != null && !problems.isEmpty()) {
                problemDialogRequested = false;
                showProblemDialog(problems);
            }
        });
        viewModel.getDrafts().observe(this, drafts -> {
            if (openDraftRequested) {
                openDraftRequested = false;
                showDraftListDialog(drafts);
            }
        });
        viewModel.getOpenedDraft().observe(this, this::populateOpenedDraft);
        viewModel.getNodes().observe(this, nodes -> updateCanvas());
        viewModel.getEdges().observe(this, edges -> updateCanvas());
        viewModel.getSelectedNodeId().observe(this, nodeId -> {
            updateCanvas();
            renderSelectedConfig();
        });
        viewModel.getCurrentFrameId().observe(this, frameId -> {
            updateCanvas();
            layoutConfigPanel.setVisibility(View.GONE);
        });
        viewModel.getSelectedEdgeId().observe(this, edgeId -> {
            updateCanvas();
            renderSelectedConfig();
        });
        viewModel.getCanvasConfigVisible().observe(this, visible -> {
            if (visible != null && visible) renderCanvasConfig();
        });
        viewModel.getConnectMode().observe(this, enabled -> {
            boolean isEnabled = enabled != null && enabled;
            String type = viewModel.getConnectModeType().getValue();
            btnConnectMode.setText(isEnabled
                    ? ("parallel".equals(type) ? "= link" : "Linking")
                    : "-> link");
            btnConnectMode.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    Color.parseColor(isEnabled ? "#0F766E" : "#334155")
            ));
            updateCanvas();
        });
        viewModel.getConnectModeType().observe(this, type -> {
            Boolean enabled = viewModel.getConnectMode().getValue();
            boolean isEnabled = enabled != null && enabled;
            btnConnectMode.setText(isEnabled
                    ? ("parallel".equals(type) ? "= link" : "Linking")
                    : "-> link");
            updateCanvas();
        });
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getLoading().observe(this, loading -> {
            boolean isLoading = loading != null && loading;
            btnSaveDraft.setEnabled(!isLoading);
            btnSubmitReview.setEnabled(!isLoading);
            btnApplyConfig.setEnabled(!isLoading);
            btnSaveDraft.setText(isLoading ? "Saving" : "Save");
        });
        viewModel.getMediaUploading().observe(this, uploading -> {
            boolean active = Boolean.TRUE.equals(uploading);
            btnPickPrimaryAsset.setEnabled(!active);
            btnPickBackgroundSound.setEnabled(!active);
            if (active) btnPickPrimaryAsset.setText("Uploading...");
            else if ("Uploading...".contentEquals(btnPickPrimaryAsset.getText())) {
                btnPickPrimaryAsset.setText("Pick media");
            }
        });
        viewModel.getUploadedMediaUrl().observe(this, url -> {
            if (url == null || url.trim().isEmpty()) return;
            etPrimaryConfig.setText(url);
            btnPickPrimaryAsset.setEnabled(true);
            btnPickPrimaryAsset.setText("Media uploaded");
        });
        viewModel.getUploadedBackgroundSoundUrl().observe(this, url -> {
            if (url == null || url.trim().isEmpty()) return;
            etBackgroundSoundUrl.setText(url);
            btnPickBackgroundSound.setEnabled(true);
            btnPickBackgroundSound.setText("Background music uploaded");
        });
    }

    private void initActions() {
        btnConnectMode.setOnClickListener(v -> viewModel.toggleConnectMode());
        btnAiMetadata.setOnClickListener(v -> requestProblemDialog());
        btnOpenDraft.setOnClickListener(v -> {
            openDraftRequested = true;
            viewModel.loadDrafts(getAuthToken());
        });
        btnApplyConfig.setOnClickListener(v -> applyConfig());
        btnDesignPosition.setOnClickListener(v -> openSceneDesigner());
        btnOpenFrame.setOnClickListener(v -> viewModel.openSelectedFrame());
        btnRemoveNode.setOnClickListener(v -> {
            View focused = getCurrentFocus();
            if (focused != null) {
                InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                focused.clearFocus();
            }
            QuestDraftRequest.QuestFlowEdge selectedEdge = viewModel.getSelectedEdge();
            if (selectedEdge != null) viewModel.removeEdge(selectedEdge.client_edge_id);
            else viewModel.removeSelectedNode();
            layoutConfigPanel.setVisibility(View.GONE);
        });
        btnPreviewQuest.setOnClickListener(v -> openPreview());
        btnPickPrimaryAsset.setOnClickListener(v -> imagePickerLauncher.launch(mediaMimeForSelectedNode()));
        btnPickBackgroundSound.setOnClickListener(v -> backgroundSoundPickerLauncher.launch("audio/*"));
        btnSaveDraft.setOnClickListener(v -> viewModel.saveDraft(
                getAuthToken(),
                etQuestTitle.getText().toString(),
                etQuestDescription.getText().toString(),
                parseLevel()
        ));
        btnSubmitReview.setOnClickListener(v -> viewModel.submitReview(getAuthToken()));
    }

    private void requestProblemDialog() {
        List<QuestProblem> problems = viewModel.getProblems().getValue();
        if (problems == null || problems.isEmpty()) {
            problemDialogRequested = true;
            Toast.makeText(this, "Đang tải danh sách vấn đề...", Toast.LENGTH_SHORT).show();
            viewModel.loadProblems(getAuthToken());
            return;
        }
        problemDialogRequested = false;
        showProblemDialog(problems);
    }

    private void showProblemDialog(List<QuestProblem> problems) {
        View content = getLayoutInflater().inflate(R.layout.dialog_quest_ai_metadata, null);
        CheckBox generalCheck = content.findViewById(R.id.checkGeneralQuest);
        Spinner level1Spinner = content.findViewById(R.id.spinnerProblemLevel1);
        Spinner level2Spinner = content.findViewById(R.id.spinnerProblemLevel2);
        Spinner level3Spinner = content.findViewById(R.id.spinnerProblemLevel3);

        QuestProblem selectedLeaf = findProblem(problems, viewModel.getProblemId());
        QuestProblem selectedGroup = selectedLeaf == null
                ? null : findProblem(problems, selectedLeaf.getParentId());
        QuestProblem selectedRoot = selectedGroup == null
                ? null : findProblem(problems, selectedGroup.getParentId());

        List<QuestProblem> roots = problemsAtLevel(problems, 1, null);
        setProblemSpinner(level1Spinner, roots, selectedRoot == null ? null : selectedRoot.getId());
        QuestProblem activeRoot = selectedProblem(level1Spinner);
        List<QuestProblem> groups = problemsAtLevel(
                problems, 2, activeRoot == null ? null : activeRoot.getId());
        setProblemSpinner(level2Spinner, groups, selectedGroup == null ? null : selectedGroup.getId());
        QuestProblem activeGroup = selectedProblem(level2Spinner);
        setProblemSpinner(level3Spinner,
                problemsAtLevel(problems, 3, activeGroup == null ? null : activeGroup.getId()),
                selectedLeaf == null ? null : selectedLeaf.getId());

        generalCheck.setChecked(viewModel.isGeneralQuest());
        boolean initialGeneral = viewModel.isGeneralQuest();
        level1Spinner.setEnabled(!initialGeneral);
        level2Spinner.setEnabled(!initialGeneral);
        level3Spinner.setEnabled(!initialGeneral);
        generalCheck.setOnCheckedChangeListener((btn, checked) -> {
            level1Spinner.setEnabled(!checked);
            level2Spinner.setEnabled(!checked);
            level3Spinner.setEnabled(!checked);
        });

        final boolean[] initializingSelection = { true };
        level1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initializingSelection[0]) return;
                QuestProblem root = selectedProblem(level1Spinner);
                List<QuestProblem> nextGroups = problemsAtLevel(
                        problems, 2, root == null ? null : root.getId());
                setProblemSpinner(level2Spinner, nextGroups, null);
                QuestProblem group = selectedProblem(level2Spinner);
                setProblemSpinner(level3Spinner,
                        problemsAtLevel(problems, 3, group == null ? null : group.getId()), null);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        level2Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initializingSelection[0]) return;
                QuestProblem group = selectedProblem(level2Spinner);
                setProblemSpinner(level3Spinner,
                        problemsAtLevel(problems, 3, group == null ? null : group.getId()), null);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chọn vấn đề cho Quest")
                .setView(content)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Áp dụng", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            level1Spinner.post(() -> initializingSelection[0] = false);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                    if (generalCheck.isChecked()) {
                        viewModel.setGeneralQuest();
                        dialog.dismiss();
                        return;
                    }
                    QuestProblem problem = selectedProblem(level3Spinner);
                    if (problem == null || !problem.isLeafNode()) {
                        Toast.makeText(this, "Hãy chọn vấn đề cụ thể ở cấp 3", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.updateAiMetadata(
                            problem,
                            new ArrayList<>(), 1, 5, "", 0
                    );
                    dialog.dismiss();
                });
        });
        dialog.show();
    }

    private List<QuestProblem> problemsAtLevel(List<QuestProblem> problems, int level, String parentId) {
        List<QuestProblem> result = new ArrayList<>();
        for (QuestProblem problem : problems) {
            if (problem.getTreeLevel() != level) continue;
            String actualParent = problem.getParentId();
            if (parentId == null ? actualParent == null : parentId.equals(actualParent)) {
                result.add(problem);
            }
        }
        return result;
    }

    private QuestProblem findProblem(List<QuestProblem> problems, String id) {
        if (id == null) return null;
        for (QuestProblem problem : problems) {
            if (id.equals(problem.getId())) return problem;
        }
        return null;
    }

    private void setProblemSpinner(Spinner spinner, List<QuestProblem> items, String selectedId) {
        spinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, items));
        if (selectedId == null) return;
        for (int index = 0; index < items.size(); index++) {
            if (selectedId.equals(items.get(index).getId())) {
                spinner.setSelection(index, false);
                return;
            }
        }
    }

    private QuestProblem selectedProblem(Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item instanceof QuestProblem ? (QuestProblem) item : null;
    }

    private List<String> parseAiTags(String rawTags) {
        List<String> tags = new ArrayList<>();
        if (rawTags == null) return tags;
        for (String value : rawTags.split(",")) {
            String tag = value.trim().toLowerCase();
            if (!tag.isEmpty() && !tags.contains(tag)) tags.add(tag);
            if (tags.size() == 20) break;
        }
        return tags;
    }

    private int parseDialogInt(EditText field, int fallback) {
        try {
            return Integer.parseInt(field.getText().toString().trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void showDraftListDialog(List<QuestDraftSummary> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            Toast.makeText(this, "No saved drafts found", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[drafts.size()];
        for (int index = 0; index < drafts.size(); index++) {
            labels[index] = drafts.get(index).toString();
        }
        new AlertDialog.Builder(this)
                .setTitle("Open saved draft")
                .setItems(labels, (dialog, which) -> {
                    QuestDraftSummary selected = drafts.get(which);
                    if (selected.latest_version_id == null) {
                        Toast.makeText(this, "This draft has no saved version", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.openDraft(getAuthToken(), selected.latest_version_id);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void populateOpenedDraft(QuestDraftDetail draft) {
        if (draft == null) return;
        etQuestTitle.setText(draft.quest_title == null ? "" : draft.quest_title);
        etQuestDescription.setText(draft.quest_description == null ? "" : draft.quest_description);
        etQuestLevel.setText(String.valueOf(Math.max(1, draft.quest_level)));
        layoutConfigPanel.setVisibility(View.GONE);
        questCanvas.requestFocus();
        updateCanvas();
    }

    private void openPreview() {
        List<QuestDraftRequest.QuestFlowNode> nodes = viewModel.getNodes().getValue();
        if (nodes == null || nodes.isEmpty()) {
            Toast.makeText(this, "Drag at least one engine onto the canvas to preview", Toast.LENGTH_SHORT).show();
            return;
        }

        QuestPreviewStore.set(
                etQuestTitle.getText().toString(),
                viewModel.getBackgroundUrl(),
                viewModel.getBackgroundColor(),
                viewModel.getBackgroundSoundUrl(),
                viewModel.getBackgroundSoundVolume(),
                nodes,
                viewModel.getEdges().getValue()
        );
        startActivity(new Intent(this, QuestPreviewActivity.class));
    }

    private void renderEnginePanel(List<QuestEngine> engines) {
        layoutEngineButtons.removeAllViews();
        if (engines == null) return;

        for (QuestEngine engine : engines) {
            TextView item = new TextView(this);
            item.setText(symbolFor(engine) + "\n" + shortNameFor(engine));
            item.setGravity(Gravity.CENTER);
            item.setTextColor(Color.WHITE);
            item.setTextSize(11);
            item.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            item.setBackground(makeToolBackground(colorFor(engine)));
            item.setPadding(4, 4, 4, 4);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(58)
            );
            params.setMargins(0, 0, 0, dp(8));
            item.setLayoutParams(params);

            item.setOnLongClickListener(v -> {
                if ("sequential".equals(engine.getEngineSubtype())) {
                    viewModel.toggleConnectMode();
                    return true;
                }
                if ("parallel".equals(engine.getEngineSubtype())) {
                    viewModel.toggleParallelConnectMode();
                    return true;
                }
                ClipData data = ClipData.newPlainText("engine", engine.getEngineSubtype());
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(data, shadowBuilder, engine, 0);
                return true;
            });

            item.setOnClickListener(v -> {
                if ("sequential".equals(engine.getEngineSubtype())) {
                    viewModel.toggleConnectMode();
                } else if ("parallel".equals(engine.getEngineSubtype())) {
                    viewModel.toggleParallelConnectMode();
                } else {
                    Toast.makeText(this, "Hold and drag an engine onto the white canvas", Toast.LENGTH_SHORT).show();
                }
            });

            layoutEngineButtons.addView(item);
        }
    }

    private void updateCanvas() {
        if (questCanvas == null) return;
        List<QuestDraftRequest.QuestFlowNode> nodes = viewModel.getNodes().getValue();
        List<QuestDraftRequest.QuestFlowEdge> edges = viewModel.getEdges().getValue();
        String currentFrameId = viewModel.getCurrentFrameId().getValue();
        List<QuestDraftRequest.QuestFlowNode> visibleNodes = visibleNodesForFrame(nodes, currentFrameId);
        List<QuestDraftRequest.QuestFlowEdge> visibleEdges = visibleEdgesForNodes(edges, visibleNodes);
        String selectedId = viewModel.getSelectedNodeId().getValue();
        String selectedEdgeId = viewModel.getSelectedEdgeId().getValue();
        Boolean isConnectMode = viewModel.getConnectMode().getValue();
        String connectModeType = viewModel.getConnectModeType().getValue();
        String backgroundUrl = currentFrameId == null
                ? viewModel.getBackgroundUrl()
                : frameBackgroundUrl(currentFrameId);
        questCanvas.setBackgroundConfig(viewModel.getBackgroundColor(), backgroundUrl);
        questCanvas.setData(visibleNodes, visibleEdges, selectedId, selectedEdgeId,
                isConnectMode != null && isConnectMode, connectModeType);
        questCanvas.setCountNodes(nodes);
    }

    private String frameBackgroundUrl(String frameId) {
        List<QuestDraftRequest.QuestFlowNode> nodes = viewModel.getNodes().getValue();
        if (nodes == null) return "";
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            if (!frameId.equals(node.client_node_id) || node.config == null) continue;
            Object value = node.config.get("background_url");
            if (value == null || "null".equals(String.valueOf(value))) return "";
            return String.valueOf(value);
        }
        return "";
    }

    private List<QuestDraftRequest.QuestFlowNode> visibleNodesForFrame(
            List<QuestDraftRequest.QuestFlowNode> nodes,
            String frameId
    ) {
        List<QuestDraftRequest.QuestFlowNode> result = new ArrayList<>();
        if (nodes == null) return result;
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            boolean visible = frameId == null
                    ? node.parent_client_node_id == null
                    : frameId.equals(node.parent_client_node_id);
            if (visible) result.add(node);
        }
        return result;
    }

    private List<QuestDraftRequest.QuestFlowEdge> visibleEdgesForNodes(
            List<QuestDraftRequest.QuestFlowEdge> edges,
            List<QuestDraftRequest.QuestFlowNode> visibleNodes
    ) {
        List<QuestDraftRequest.QuestFlowEdge> result = new ArrayList<>();
        if (edges == null || visibleNodes == null) return result;
        Set<String> visibleIds = new HashSet<>();
        for (QuestDraftRequest.QuestFlowNode node : visibleNodes) visibleIds.add(node.client_node_id);
        for (QuestDraftRequest.QuestFlowEdge edge : edges) {
            if (visibleIds.contains(edge.source_client_node_id)
                    && visibleIds.contains(edge.target_client_node_id)) {
                result.add(edge);
            }
        }
        return result;
    }

    private void renderSelectedConfig() {
        QuestDraftRequest.QuestFlowEdge edge = viewModel.getSelectedEdge();
        if (edge != null) {
            renderEdgeConfig(edge);
            return;
        }
        QuestDraftRequest.QuestFlowNode node = viewModel.getSelectedNode();
        if (node == null) {
            layoutConfigPanel.setVisibility(View.GONE);
            return;
        }

        layoutConfigPanel.setVisibility(View.VISIBLE);
        btnRemoveNode.setVisibility(View.VISIBLE);
        btnRemoveNode.setText("Delete engine");
        btnDesignPosition.setVisibility(isSceneVisual(node.engine_subtype) ? View.VISIBLE : View.GONE);
        btnOpenFrame.setVisibility("composite".equals(node.engine_subtype) ? View.VISIBLE : View.GONE);
        tvConfigTitle.setText("Config: " + node.engine_subtype);
        etNodeName.setVisibility(View.VISIBLE);
        etNodeName.setText(node.display_name == null ? "" : node.display_name);
        configurePanelFor(node.engine_subtype);

        Map<String, Object> config = node.config;
        if (config == null || config.isEmpty()) {
            return;
        }

        switch (node.engine_subtype) {
            case "text":
                setConfigText(config.get("text"), config.get("size"));
                break;
            case "timer":
                setConfigText(config.get("duration_seconds"), config.get("size"));
                break;
            case "gesture":
                setGestureSelection(String.valueOf(config.get("gesture_type")));
                setConfigText("", gestureValueFromConfig(config));
                break;
            case "sensor":
                setSpinnerSelection(spinnerSensorAction, config.get("sensor_action"), "face_down");
                setConfigText("", config.get("duration_seconds"));
                break;
            case "voice":
                setSpinnerSelection(spinnerVoiceMode, config.get("mode"), "breath");
                setConfigText("", config.get("duration_seconds"));
                break;
            case "text_input":
                setConfigText(config.get("placeholder"), config.get("word_limit"));
                break;
            case "parallel":
                setSpinnerSelection(spinnerParallelCondition,
                        config.get("completion_condition"), "A_OR_B");
                break;
            case "composite":
                setConfigText(config.get("background_url"), "");
                break;
            case "quest":
                setConfigText(config.get("nested_title"), config.get("note"));
                break;
            case "audio":
                setConfigText(config.get("asset_url"), config.get("volume"));
                break;
            case "image":
                setConfigText(config.get("asset_url"), "");
                setSpinnerSelection(spinnerMediaPosition, config.get("position"), "center");
                etImageWidth.setText(String.valueOf(config.get("width") == null ? "240" : config.get("width")));
                etImageHeight.setText(String.valueOf(config.get("height") == null ? "180" : config.get("height")));
                break;
            case "video":
                setConfigText(config.get("asset_url"), "");
                break;
            default:
                setConfigText(config.get("asset_url"), config.get("position"));
                break;
        }

        renderAttachedEngineConfig(config);
    }

    private void renderEdgeConfig(QuestDraftRequest.QuestFlowEdge edge) {
        layoutConfigPanel.setVisibility(View.VISIBLE);
        boolean isParallel = "parallel".equals(edge.flow_type);
        tvConfigTitle.setText(isParallel
                ? "Config: Parallel link\nA: " + nodeLabel(edge.source_client_node_id)
                + "   B: " + nodeLabel(edge.target_client_node_id)
                : "Config: Sequence arrow");
        etNodeName.setVisibility(View.GONE);
        hideGestureType();
        hideAttachedEngine();
        hideSpecialConfigControls();
        clearConfigInputs();
        if (isParallel) {
            spinnerParallelCondition.setVisibility(View.VISIBLE);
        } else {
            showTransitionControls();
        }
        btnRemoveNode.setVisibility(View.VISIBLE);
        btnRemoveNode.setText(isParallel ? "Delete parallel link" : "Delete arrow");
        btnDesignPosition.setVisibility(View.GONE);
        btnOpenFrame.setVisibility(View.GONE);

        Map<String, Object> config = edge.config;
        if (isParallel) {
            Object condition = edge.completion_condition != null
                    ? edge.completion_condition
                    : config == null ? null : config.get("completion_condition");
            setSpinnerSelection(spinnerParallelCondition, condition, "A_OR_B");
            etPrimaryConfig.setVisibility(View.GONE);
            etSecondaryConfig.setVisibility(View.GONE);
            return;
        }
        String type = config == null || config.get("transition_type") == null
                ? "delay" : String.valueOf(config.get("transition_type"));
        String effect = config == null || config.get("transition_effect") == null
                ? "fade" : String.valueOf(config.get("transition_effect"));
        setSpinnerSelection(spinnerTransitionType, type, "delay");
        setSpinnerSelection(spinnerTransitionEffect, effect, "fade");
        etPrimaryConfig.setText(config == null || config.get("delay_seconds") == null
                ? "3" : String.valueOf(config.get("delay_seconds")));
        etSecondaryConfig.setVisibility(View.GONE);
    }

    private void renderCanvasConfig() {
        layoutConfigPanel.setVisibility(View.VISIBLE);
        btnRemoveNode.setVisibility(View.GONE);
        btnRemoveNode.setText("Delete engine");
        btnDesignPosition.setVisibility(View.GONE);
        btnOpenFrame.setVisibility(View.GONE);
        tvConfigTitle.setText("Config: Background");
        etNodeName.setVisibility(View.GONE);
        hideGestureType();
        hideAttachedEngine();
        hideSpecialConfigControls();
        etPrimaryConfig.setVisibility(View.VISIBLE);
        btnPickPrimaryAsset.setVisibility(View.VISIBLE);
        etSecondaryConfig.setVisibility(View.VISIBLE);
        etPrimaryConfig.setHint("Selected background image");
        etSecondaryConfig.setHint("Background color HEX, e.g. #FFFFFF");
        etPrimaryConfig.setText(viewModel.getBackgroundUrl());
        etSecondaryConfig.setText(viewModel.getBackgroundColor());
        etBackgroundSoundUrl.setVisibility(View.VISIBLE);
        btnPickBackgroundSound.setVisibility(View.VISIBLE);
        etBackgroundSoundVolume.setVisibility(View.VISIBLE);
        etBackgroundSoundUrl.setText(viewModel.getBackgroundSoundUrl());
        etBackgroundSoundVolume.setText(String.valueOf(viewModel.getBackgroundSoundVolume()));
    }

    private String nodeLabel(String nodeId) {
        List<QuestDraftRequest.QuestFlowNode> nodes = viewModel.getNodes().getValue();
        if (nodes == null) return "Engine";
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            if (!nodeId.equals(node.client_node_id)) continue;
            if (node.display_name != null && !node.display_name.trim().isEmpty()) {
                return node.display_name.trim();
            }
            return node.engine_subtype == null ? "Engine" : node.engine_subtype;
        }
        return "Engine";
    }

    private void applyConfig() {
        Boolean canvasVisible = viewModel.getCanvasConfigVisible().getValue();
        if (canvasVisible != null && canvasVisible && viewModel.getSelectedNodeId().getValue() == null) {
            viewModel.updateCanvasConfig(
                    etPrimaryConfig.getText().toString(),
                    etSecondaryConfig.getText().toString(),
                    etBackgroundSoundUrl.getText().toString(),
                    etBackgroundSoundVolume.getText().toString()
            );
            Toast.makeText(this, "Background configuration applied", Toast.LENGTH_SHORT).show();
            return;
        }

        if (viewModel.getSelectedEdge() != null) {
            QuestDraftRequest.QuestFlowEdge edge = viewModel.getSelectedEdge();
            if (edge != null && "parallel".equals(edge.flow_type)) {
                viewModel.updateSelectedParallelEdge(selectedSpinnerValue(spinnerParallelCondition, "A_OR_B"));
            } else {
                viewModel.updateSelectedEdge(
                        selectedSpinnerValue(spinnerTransitionType, "delay"),
                        etPrimaryConfig.getText().toString(),
                        selectedSpinnerValue(spinnerTransitionEffect, "none")
                );
            }
            return;
        }

        viewModel.updateSelectedNode(
                etNodeName.getText().toString(),
                effectivePrimaryConfig(),
                effectiveSecondaryConfig(),
                selectedAttachedEngineType(),
                effectiveAttachedPrimaryConfig(),
                effectiveAttachedSecondaryConfig()
        );
        QuestDraftRequest.QuestFlowNode selectedNode = viewModel.getSelectedNode();
        if (selectedNode != null && "image".equals(selectedNode.engine_subtype)) {
            viewModel.updateSelectedImageLayout(
                    selectedSpinnerValue(spinnerMediaPosition, "center"),
                    etImageWidth.getText().toString(),
                    etImageHeight.getText().toString()
            );
        }
    }

    private void openSceneDesigner() {
        QuestDraftRequest.QuestFlowNode selected = viewModel.getSelectedNode();
        if (selected == null || !isSceneVisual(selected.engine_subtype)) return;
        List<QuestDraftRequest.QuestFlowNode> sceneNodes = new ArrayList<>();
        List<QuestDraftRequest.QuestFlowNode> allNodes = viewModel.getNodes().getValue();
        if (allNodes != null) {
            for (QuestDraftRequest.QuestFlowNode node : allNodes) {
                boolean sameScene = selected.parent_client_node_id == null
                        ? node.parent_client_node_id == null
                        : selected.parent_client_node_id.equals(node.parent_client_node_id);
                if (sameScene) sceneNodes.add(node);
            }
        }

        String background = viewModel.getBackgroundUrl();
        if (selected.parent_client_node_id != null && allNodes != null) {
            for (QuestDraftRequest.QuestFlowNode node : allNodes) {
                if (!selected.parent_client_node_id.equals(node.client_node_id)
                        || node.config == null || node.config.get("background_url") == null) continue;
                String frameBackground = String.valueOf(node.config.get("background_url"));
                if (!frameBackground.trim().isEmpty() && !"null".equals(frameBackground)) {
                    background = frameBackground;
                }
                break;
            }
        }
        QuestSceneDesignStore.set(selected.client_node_id, background,
                viewModel.getBackgroundColor(), sceneNodes);
        sceneDesignerLauncher.launch(new Intent(this, QuestSceneDesignerActivity.class));
    }

    private boolean isSceneVisual(String subtype) {
        return "text".equals(subtype) || "image".equals(subtype) || "video".equals(subtype)
                || "timer".equals(subtype) || "gesture".equals(subtype)
                || "text_input".equals(subtype);
    }

    private void handlePickedImage(Uri uri) {
        copyAndUploadMedia(uri, false);
    }

    private void handlePickedBackgroundSound(Uri uri) {
        copyAndUploadMedia(uri, true);
    }

    private void copyAndUploadMedia(Uri uri, boolean backgroundSound) {
        if (uri == null) return;

        try {
            File dir = new File(getFilesDir(), "quest_assets");
            if (!dir.exists()) dir.mkdirs();

            String mimeType = getContentResolver().getType(uri);
            String extension = extensionForMimeType(mimeType);
            File outFile = new File(dir, "quest_asset_" + System.currentTimeMillis() + extension);
            try (InputStream input = getContentResolver().openInputStream(uri);
                 FileOutputStream output = new FileOutputStream(outFile)) {
                if (input == null) {
                    Toast.makeText(this, "Cannot read image", Toast.LENGTH_SHORT).show();
                    return;
                }

                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            }

            String resolvedMime = mimeType == null ? "application/octet-stream" : mimeType;
            if (backgroundSound) viewModel.uploadBackgroundSound(getAuthToken(), outFile, resolvedMime);
            else viewModel.uploadMedia(getAuthToken(), outFile, resolvedMime);
        } catch (Exception error) {
            Toast.makeText(this, "Media selection failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String mediaMimeForSelectedNode() {
        QuestDraftRequest.QuestFlowNode node = viewModel.getSelectedNode();
        if (node == null) return "image/*";
        // The backend accepts MP4 only, so do not offer formats it will reject.
        if ("video".equals(node.engine_subtype)) return "video/mp4";
        if ("audio".equals(node.engine_subtype)) return "audio/*";
        return "image/*";
    }

    private String extensionForMimeType(String mimeType) {
        if (mimeType == null) return ".bin";
        if (mimeType.contains("png")) return ".png";
        if (mimeType.contains("webp")) return ".webp";
        if (mimeType.contains("svg")) return ".svg";
        if (mimeType.startsWith("video/")) return ".mp4";
        if (mimeType.contains("mpeg")) return ".mp3";
        if (mimeType.contains("wav")) return ".wav";
        return mimeType.startsWith("audio/") ? ".audio" : ".jpg";
    }

    @Override
    public void onNodeSelected(String nodeId) {
        viewModel.selectNode(nodeId);
    }

    @Override
    public void onCanvasSelected() {
        String currentFrameId = viewModel.getCurrentFrameId().getValue();
        if (currentFrameId == null) viewModel.selectCanvas();
        else viewModel.selectNode(currentFrameId);
    }

    @Override
    public void onNodeMoved(String nodeId, float x, float y) {
        viewModel.moveNode(nodeId, x, y);
    }

    @Override
    public void onSequentialEdgeCreated(String sourceNodeId, String targetNodeId) {
        viewModel.addSequentialEdge(sourceNodeId, targetNodeId);
    }

    @Override
    public void onEdgeSelected(String edgeId) {
        viewModel.selectEdge(edgeId);
    }

    private void configurePanelFor(String subtype) {
        etPrimaryConfig.setVisibility(View.VISIBLE);
        btnPickPrimaryAsset.setVisibility(View.GONE);
        etSecondaryConfig.setVisibility(View.VISIBLE);
        hideGestureType();
        hideAttachedEngine();
        hideSpecialConfigControls();
        clearConfigInputs();

        switch (subtype) {
            case "text":
                etPrimaryConfig.setHint("Display text");
                etSecondaryConfig.setHint("Text size, e.g. 22");
                showAttachedEngine();
                break;
            case "timer":
                etPrimaryConfig.setHint("Duration in seconds");
                etSecondaryConfig.setHint("Number size, e.g. 48");
                break;
            case "image":
                etPrimaryConfig.setHint("Selected media file");
                btnPickPrimaryAsset.setText("Pick image");
                btnPickPrimaryAsset.setVisibility(View.VISIBLE);
                etSecondaryConfig.setVisibility(View.GONE);
                spinnerMediaPosition.setVisibility(View.VISIBLE);
                layoutImageSize.setVisibility(View.VISIBLE);
                etImageWidth.setText("240");
                etImageHeight.setText("180");
                showAttachedEngine();
                break;
            case "video":
                etPrimaryConfig.setHint("Selected video file");
                btnPickPrimaryAsset.setText("Pick video");
                btnPickPrimaryAsset.setVisibility(View.VISIBLE);
                etSecondaryConfig.setVisibility(View.GONE);
                break;
            case "audio":
                etPrimaryConfig.setHint("Selected audio file");
                btnPickPrimaryAsset.setText("Pick audio");
                btnPickPrimaryAsset.setVisibility(View.VISIBLE);
                etSecondaryConfig.setHint("Volume 0-100");
                break;
            case "gesture":
                etPrimaryConfig.setVisibility(View.GONE);
                spinnerGestureType.setVisibility(View.VISIBLE);
                updateGestureConfigHint("tap", true);
                break;
            case "sensor":
                etPrimaryConfig.setVisibility(View.GONE);
                spinnerSensorAction.setVisibility(View.VISIBLE);
                etSecondaryConfig.setHint("Duration in seconds");
                break;
            case "voice":
                etPrimaryConfig.setVisibility(View.GONE);
                spinnerVoiceMode.setVisibility(View.VISIBLE);
                etSecondaryConfig.setHint("Recording duration in seconds");
                break;
            case "text_input":
                etPrimaryConfig.setHint("Placeholder / question");
                etSecondaryConfig.setHint("Word limit");
                break;
            case "parallel":
                etPrimaryConfig.setVisibility(View.GONE);
                etSecondaryConfig.setVisibility(View.GONE);
                spinnerParallelCondition.setVisibility(View.VISIBLE);
                break;
            case "composite":
                etPrimaryConfig.setHint("Selected frame background image");
                btnPickPrimaryAsset.setText("Pick frame background");
                btnPickPrimaryAsset.setVisibility(View.VISIBLE);
                etSecondaryConfig.setVisibility(View.GONE);
                break;
            case "quest":
                etPrimaryConfig.setHint("Sub quest title");
                etSecondaryConfig.setHint("Sub quest note");
                break;
            default:
                etPrimaryConfig.setHint("Primary value");
                etSecondaryConfig.setHint("Secondary value");
                break;
        }
    }

    private void clearConfigInputs() {
        etPrimaryConfig.setText("");
        etSecondaryConfig.setText("");
        etAttachedPrimaryConfig.setText("");
        etAttachedSecondaryConfig.setText("");
    }

    private void hideSpecialConfigControls() {
        spinnerMediaPosition.setVisibility(View.GONE);
        spinnerSensorAction.setVisibility(View.GONE);
        spinnerVoiceMode.setVisibility(View.GONE);
        spinnerParallelCondition.setVisibility(View.GONE);
        layoutImageSize.setVisibility(View.GONE);
        etBackgroundSoundUrl.setVisibility(View.GONE);
        btnPickBackgroundSound.setVisibility(View.GONE);
        etBackgroundSoundVolume.setVisibility(View.GONE);
        hideTransitionControls();
    }

    private void showTransitionControls() {
        tvTransitionTypeLabel.setVisibility(View.VISIBLE);
        spinnerTransitionType.setVisibility(View.VISIBLE);
        tvTransitionEffectLabel.setVisibility(View.VISIBLE);
        spinnerTransitionEffect.setVisibility(View.VISIBLE);
        etPrimaryConfig.setVisibility(View.VISIBLE);
        btnPickPrimaryAsset.setVisibility(View.GONE);
    }

    private void hideTransitionControls() {
        tvTransitionTypeLabel.setVisibility(View.GONE);
        spinnerTransitionType.setVisibility(View.GONE);
        tvTransitionEffectLabel.setVisibility(View.GONE);
        spinnerTransitionEffect.setVisibility(View.GONE);
    }

    private void hideGestureType() {
        spinnerGestureType.setVisibility(View.GONE);
    }

    private void showAttachedEngine() {
        tvAttachedEngineTitle.setVisibility(View.VISIBLE);
        spinnerAttachedEngine.setVisibility(View.VISIBLE);
        setAttachedEngineSelection("none");
    }

    private void hideAttachedEngine() {
        tvAttachedEngineTitle.setVisibility(View.GONE);
        spinnerAttachedEngine.setVisibility(View.GONE);
        spinnerAttachedGestureType.setVisibility(View.GONE);
        spinnerAttachedSwipeDirection.setVisibility(View.GONE);
        etAttachedPrimaryConfig.setVisibility(View.GONE);
        etAttachedSecondaryConfig.setVisibility(View.GONE);
    }

    private void updateGestureConfigHint(String gestureType, boolean clearValue) {
        if (clearValue) etSecondaryConfig.setText("");
        etSecondaryConfig.setVisibility(View.VISIBLE);
        if ("tap".equals(gestureType)) {
            etSecondaryConfig.setHint("Visible tap label; blank = full screen");
        } else if ("spam_tap".equals(gestureType)) {
            etSecondaryConfig.setHint("Tap count, e.g. 3");
        } else if ("swipe".equals(gestureType)) {
            etSecondaryConfig.setHint("Direction: up, down, left, right");
        } else if ("hold".equals(gestureType)) {
            etSecondaryConfig.setHint("Hold duration in seconds");
        }
    }

    private void updateAttachedEngineFields(String engineType, boolean clearValue) {
        if (clearValue) {
            etAttachedPrimaryConfig.setText("");
            etAttachedSecondaryConfig.setText("");
        }

        boolean hasAttached = engineType != null && !"none".equals(engineType);
        etAttachedPrimaryConfig.setVisibility(hasAttached ? View.VISIBLE : View.GONE);
        etAttachedSecondaryConfig.setVisibility(hasAttached ? View.VISIBLE : View.GONE);
        spinnerAttachedGestureType.setVisibility(View.GONE);
        spinnerAttachedSwipeDirection.setVisibility(View.GONE);

        if (!hasAttached) return;

        switch (engineType) {
            case "timer":
                etAttachedPrimaryConfig.setHint("Duration in seconds");
                etAttachedSecondaryConfig.setText("");
                etAttachedSecondaryConfig.setVisibility(View.GONE);
                break;
            case "gesture":
                etAttachedPrimaryConfig.setVisibility(View.GONE);
                spinnerAttachedGestureType.setVisibility(View.VISIBLE);
                updateAttachedGestureFields(
                        String.valueOf(spinnerAttachedGestureType.getSelectedItem()),
                        clearValue
                );
                break;
            case "sensor":
                etAttachedPrimaryConfig.setVisibility(View.GONE);
                spinnerSensorAction.setVisibility(View.VISIBLE);
                etAttachedSecondaryConfig.setHint("Duration in seconds");
                break;
            case "voice":
                etAttachedPrimaryConfig.setVisibility(View.GONE);
                spinnerVoiceMode.setVisibility(View.VISIBLE);
                etAttachedSecondaryConfig.setHint("Recording duration in seconds");
                break;
            case "text_input":
                etAttachedPrimaryConfig.setHint("Placeholder / question");
                etAttachedSecondaryConfig.setHint("Word limit");
                break;
            default:
                etAttachedPrimaryConfig.setHint("Primary config");
                etAttachedSecondaryConfig.setHint("Secondary config");
                break;
        }
    }

    private String effectivePrimaryConfig() {
        QuestDraftRequest.QuestFlowNode node = viewModel.getSelectedNode();
        if (node != null && "gesture".equals(node.engine_subtype)) {
            return String.valueOf(spinnerGestureType.getSelectedItem());
        }
        if (node != null && "sensor".equals(node.engine_subtype)) {
            return selectedSpinnerValue(spinnerSensorAction, "face_down");
        }
        if (node != null && "voice".equals(node.engine_subtype)) {
            return selectedSpinnerValue(spinnerVoiceMode, "breath");
        }
        if (node != null && "parallel".equals(node.engine_subtype)) {
            return selectedSpinnerValue(spinnerParallelCondition, "A_OR_B");
        }
        return etPrimaryConfig.getText().toString();
    }

    private String effectiveSecondaryConfig() {
        QuestDraftRequest.QuestFlowNode node = viewModel.getSelectedNode();
        if (node != null && "image".equals(node.engine_subtype)) {
            return selectedSpinnerValue(spinnerMediaPosition, "center");
        }
        return etSecondaryConfig.getVisibility() == View.VISIBLE
                ? etSecondaryConfig.getText().toString()
                : "";
    }

    private String selectedAttachedEngineType() {
        Object selected = spinnerAttachedEngine.getSelectedItem();
        return selected == null ? "none" : String.valueOf(selected);
    }

    private String effectiveAttachedPrimaryConfig() {
        String attachedType = selectedAttachedEngineType();
        if ("gesture".equals(attachedType)) {
            Object selected = spinnerAttachedGestureType.getSelectedItem();
            return selected == null ? "tap" : String.valueOf(selected);
        }
        if ("sensor".equals(attachedType)) {
            return selectedSpinnerValue(spinnerSensorAction, "face_down");
        }
        if ("voice".equals(attachedType)) {
            return selectedSpinnerValue(spinnerVoiceMode, "breath");
        }
        return etAttachedPrimaryConfig.getText().toString();
    }

    private String effectiveAttachedSecondaryConfig() {
        if ("gesture".equals(selectedAttachedEngineType())
                && "swipe".equals(effectiveAttachedPrimaryConfig())) {
            Object selected = spinnerAttachedSwipeDirection.getSelectedItem();
            return selected == null ? "right" : String.valueOf(selected);
        }
        return etAttachedSecondaryConfig.getVisibility() == View.VISIBLE
                ? etAttachedSecondaryConfig.getText().toString()
                : "";
    }

    private void updateAttachedGestureFields(String gestureType, boolean clearValue) {
        if (clearValue) etAttachedSecondaryConfig.setText("");
        spinnerAttachedSwipeDirection.setVisibility(View.GONE);
        etAttachedSecondaryConfig.setVisibility(View.GONE);

        if ("spam_tap".equals(gestureType)) {
            etAttachedSecondaryConfig.setVisibility(View.VISIBLE);
            etAttachedSecondaryConfig.setHint("Required tap count");
        } else if ("swipe".equals(gestureType)) {
            spinnerAttachedSwipeDirection.setVisibility(View.VISIBLE);
        } else if ("hold".equals(gestureType)) {
            etAttachedSecondaryConfig.setVisibility(View.VISIBLE);
            etAttachedSecondaryConfig.setHint("Hold duration in seconds");
        }
    }

    private void setConfigText(Object primary, Object secondary) {
        etPrimaryConfig.setText(primary == null ? "" : String.valueOf(primary));
        etSecondaryConfig.setText(secondary == null ? "" : String.valueOf(secondary));
    }

    private String selectedSpinnerValue(Spinner spinner, String fallback) {
        Object selected = spinner.getSelectedItem();
        return selected == null ? fallback : String.valueOf(selected);
    }

    private void setSpinnerSelection(Spinner spinner, Object rawValue, String fallback) {
        String value = rawValue == null || "null".equals(String.valueOf(rawValue))
                ? fallback : String.valueOf(rawValue);
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equals(spinner.getItemAtPosition(i))) {
                spinner.setSelection(i);
                return;
            }
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            if (fallback.equals(spinner.getItemAtPosition(i))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void setGestureSelection(String gestureType) {
        String value = gestureType == null || "null".equals(gestureType) ? "tap" : gestureType;
        for (int i = 0; i < spinnerGestureType.getCount(); i++) {
            if (value.equals(spinnerGestureType.getItemAtPosition(i))) {
                spinnerGestureType.setSelection(i);
                updateGestureConfigHint(value, false);
                return;
            }
        }
        spinnerGestureType.setSelection(0);
        updateGestureConfigHint("tap", false);
    }

    private Object gestureValueFromConfig(Map<String, Object> config) {
        String type = String.valueOf(config.get("gesture_type"));
        if ("spam_tap".equals(type)) return config.get("required_count");
        if ("swipe".equals(type)) return config.get("direction");
        if ("hold".equals(type)) return config.get("duration_seconds");
        if ("tap".equals(type)) return config.get("action_label");
        return "";
    }

    @SuppressWarnings("unchecked")
    private void renderAttachedEngineConfig(Map<String, Object> config) {
        Object attachedObject = config.get("attached_engine");
        if (!(attachedObject instanceof Map)) {
            setAttachedEngineSelection("none");
            return;
        }

        Map<String, Object> attached = (Map<String, Object>) attachedObject;
        String type = String.valueOf(attached.get("engine_subtype"));
        setAttachedEngineSelection(type);

        switch (type) {
            case "timer":
                setAttachedConfigText(attached.get("duration_seconds"), "");
                break;
            case "gesture":
                String gestureType = String.valueOf(attached.get("gesture_type"));
                setAttachedGestureSelection(gestureType);
                if ("swipe".equals(gestureType)) {
                    setAttachedSwipeDirection(String.valueOf(attached.get("direction")));
                } else {
                    etAttachedSecondaryConfig.setText(
                            gestureValueFromConfig(attached) == null
                                    ? "" : String.valueOf(gestureValueFromConfig(attached))
                    );
                }
                break;
            case "sensor":
                setSpinnerSelection(spinnerSensorAction, attached.get("sensor_action"), "face_down");
                setAttachedConfigText("", attached.get("duration_seconds"));
                break;
            case "voice":
                setSpinnerSelection(spinnerVoiceMode, attached.get("mode"), "breath");
                setAttachedConfigText("", attached.get("duration_seconds"));
                break;
            case "text_input":
                setAttachedConfigText(attached.get("placeholder"), attached.get("word_limit"));
                break;
            default:
                setAttachedConfigText(attached.get("value"), attached.get("note"));
                break;
        }
    }

    private void setAttachedEngineSelection(String type) {
        String value = type == null || "null".equals(type) ? "none" : type;
        for (int i = 0; i < spinnerAttachedEngine.getCount(); i++) {
            if (value.equals(spinnerAttachedEngine.getItemAtPosition(i))) {
                spinnerAttachedEngine.setSelection(i);
                updateAttachedEngineFields(value, false);
                return;
            }
        }
        spinnerAttachedEngine.setSelection(0);
        updateAttachedEngineFields("none", false);
    }

    private void setAttachedConfigText(Object primary, Object secondary) {
        etAttachedPrimaryConfig.setText(primary == null ? "" : String.valueOf(primary));
        etAttachedSecondaryConfig.setText(secondary == null ? "" : String.valueOf(secondary));
    }

    private void setAttachedGestureSelection(String gestureType) {
        String value = gestureType == null || "null".equals(gestureType) ? "tap" : gestureType;
        for (int i = 0; i < spinnerAttachedGestureType.getCount(); i++) {
            if (value.equals(spinnerAttachedGestureType.getItemAtPosition(i))) {
                spinnerAttachedGestureType.setSelection(i);
                updateAttachedGestureFields(value, false);
                return;
            }
        }
        spinnerAttachedGestureType.setSelection(0);
        updateAttachedGestureFields("tap", false);
    }

    private void setAttachedSwipeDirection(String direction) {
        String value = direction == null || "null".equals(direction) ? "right" : direction;
        for (int i = 0; i < spinnerAttachedSwipeDirection.getCount(); i++) {
            if (value.equals(spinnerAttachedSwipeDirection.getItemAtPosition(i))) {
                spinnerAttachedSwipeDirection.setSelection(i);
                return;
            }
        }
        spinnerAttachedSwipeDirection.setSelection(3);
    }

    private String getAuthToken() {
        String token = prefsHelper.getToken();
        if (token == null) return "";
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private int parseLevel() {
        try {
            return Integer.parseInt(etQuestLevel.getText().toString());
        } catch (Exception ignored) {
            return 2;
        }
    }

    private String symbolFor(QuestEngine engine) {
        String subtype = engine.getEngineSubtype();
        if ("sequential".equals(subtype)) return "->";
        if ("parallel".equals(subtype)) return "=";
        if ("composite".equals(subtype)) return "[]";
        if ("quest".equals(subtype)) return "Q";
        if ("image".equals(subtype)) return "IMG";
        if ("video".equals(subtype)) return "VID";
        if ("audio".equals(subtype)) return "AUD";
        if ("gesture".equals(subtype)) return "TAP";
        if ("sensor".equals(subtype)) return "SNS";
        if ("voice".equals(subtype)) return "MIC";
        if ("text_input".equals(subtype)) return "IN";
        if ("text".equals(subtype)) return "TXT";
        if ("timer".equals(subtype)) return "60";
        return "?";
    }

    private String shortNameFor(QuestEngine engine) {
        String subtype = engine.getEngineSubtype();
        if ("sequential".equals(subtype)) return "Arrow";
        if ("parallel".equals(subtype)) return "Parallel";
        if ("composite".equals(subtype)) return "Frame";
        if ("text_input".equals(subtype)) return "Input";
        return subtype == null ? "Engine" : subtype.replace("_", " ");
    }

    private int colorFor(QuestEngine engine) {
        String subtype = engine.getEngineSubtype();
        if ("text".equals(subtype) || "timer".equals(subtype)) return Color.parseColor("#1B8A92");
        if ("image".equals(subtype) || "video".equals(subtype) || "audio".equals(subtype)) return Color.parseColor("#4F46E5");
        if ("gesture".equals(subtype) || "sensor".equals(subtype) || "voice".equals(subtype) || "text_input".equals(subtype)) return Color.parseColor("#BE123C");
        if ("quest".equals(subtype)) return Color.parseColor("#7C3AED");
        return Color.parseColor("#334155");
    }

    private GradientDrawable makeToolBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
