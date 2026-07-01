package com.example.emotiondebugging.ui.staff;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.emotiondebugging.model.request.QuestDraftRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestSceneCanvasView extends View {
    public static final float SCENE_WIDTH = 360f;
    public static final float SCENE_HEIGHT = 640f;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<QuestDraftRequest.QuestFlowNode> nodes = new ArrayList<>();
    private String selectedNodeId;
    private float sceneX;
    private float sceneY;
    private float sceneWidth;
    private float sceneHeight;
    private boolean resizing;
    private float lastSceneX;
    private float lastSceneY;

    public QuestSceneCanvasView(Context context) { super(context); init(); }
    public QuestSceneCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(2));
        borderPaint.setColor(Color.parseColor("#0D9488"));
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(12));
        textPaint.setFakeBoldText(true);
        handlePaint.setColor(Color.parseColor("#0D9488"));
    }

    public void setScene(List<QuestDraftRequest.QuestFlowNode> sceneNodes, String nodeId) {
        nodes = sceneNodes == null ? new ArrayList<>() : sceneNodes;
        selectedNodeId = nodeId;
        QuestDraftRequest.QuestFlowNode selected = selectedNode();
        sceneWidth = floatConfig(selected, "scene_width", defaultWidth(selected));
        sceneHeight = floatConfig(selected, "scene_height", defaultHeight(selected));
        sceneX = floatConfig(selected, "scene_x", (SCENE_WIDTH - sceneWidth) / 2f);
        sceneY = floatConfig(selected, "scene_y", (SCENE_HEIGHT - sceneHeight) / 2f);
        clampSelected();
        invalidate();
    }

    public float getSceneX() { return sceneX; }
    public float getSceneY() { return sceneY; }
    public float getSceneWidth() { return sceneWidth; }
    public float getSceneHeight() { return sceneHeight; }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float sx = getWidth() / SCENE_WIDTH;
        float sy = getHeight() / SCENE_HEIGHT;
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            if (!isVisual(node)) continue;
            boolean selected = node.client_node_id.equals(selectedNodeId);
            float w = selected ? sceneWidth : floatConfig(node, "scene_width", defaultWidth(node));
            float h = selected ? sceneHeight : floatConfig(node, "scene_height", defaultHeight(node));
            float x = selected ? sceneX : floatConfig(node, "scene_x", (SCENE_WIDTH - w) / 2f);
            float y = selected ? sceneY : floatConfig(node, "scene_y", (SCENE_HEIGHT - h) / 2f);
            RectF rect = new RectF(x * sx, y * sy, (x + w) * sx, (y + h) * sy);
            int color = colorFor(node.engine_subtype);
            fillPaint.setColor(color);
            fillPaint.setAlpha(selected ? 220 : 55);
            canvas.drawRoundRect(rect, dp(6), dp(6), fillPaint);
            if (selected) {
                canvas.drawRoundRect(rect, dp(6), dp(6), borderPaint);
                canvas.drawCircle(rect.right, rect.bottom, dp(9), handlePaint);
            }
            textPaint.setAlpha(selected ? 255 : 120);
            canvas.drawText(label(node), rect.left + dp(8), rect.centerY() + dp(4), textPaint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float sx = getWidth() / SCENE_WIDTH;
        float sy = getHeight() / SCENE_HEIGHT;
        float x = event.getX() / sx;
        float y = event.getY() / sy;
        RectF selectedRect = new RectF(sceneX, sceneY, sceneX + sceneWidth, sceneY + sceneHeight);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!selectedRect.contains(x, y)) return false;
                resizing = Math.abs(x - selectedRect.right) < 28f
                        && Math.abs(y - selectedRect.bottom) < 28f;
                lastSceneX = x;
                lastSceneY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = x - lastSceneX;
                float dy = y - lastSceneY;
                if (resizing) {
                    sceneWidth = Math.max(40f, sceneWidth + dx);
                    sceneHeight = Math.max(32f, sceneHeight + dy);
                } else {
                    sceneX += dx;
                    sceneY += dy;
                }
                lastSceneX = x;
                lastSceneY = y;
                clampSelected();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resizing = false;
                return true;
            default:
                return true;
        }
    }

    private void clampSelected() {
        sceneWidth = Math.min(sceneWidth, SCENE_WIDTH);
        sceneHeight = Math.min(sceneHeight, SCENE_HEIGHT);
        sceneX = Math.max(0f, Math.min(sceneX, SCENE_WIDTH - sceneWidth));
        sceneY = Math.max(0f, Math.min(sceneY, SCENE_HEIGHT - sceneHeight));
    }

    private QuestDraftRequest.QuestFlowNode selectedNode() {
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            if (node.client_node_id.equals(selectedNodeId)) return node;
        }
        return null;
    }

    private boolean isVisual(QuestDraftRequest.QuestFlowNode node) {
        String type = node.engine_subtype;
        return "text".equals(type) || "image".equals(type) || "video".equals(type)
                || "timer".equals(type) || "gesture".equals(type) || "text_input".equals(type);
    }

    private String label(QuestDraftRequest.QuestFlowNode node) {
        return node.display_name == null || node.display_name.trim().isEmpty()
                ? node.engine_subtype.toUpperCase() : node.display_name;
    }

    private float floatConfig(QuestDraftRequest.QuestFlowNode node, String key, float fallback) {
        if (node == null || node.config == null || node.config.get(key) == null) return fallback;
        try {
            Object value = node.config.get(key);
            return value instanceof Number ? ((Number) value).floatValue()
                    : Float.parseFloat(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private float defaultWidth(QuestDraftRequest.QuestFlowNode node) {
        return node != null && "image".equals(node.engine_subtype) ? 240f : 280f;
    }

    private float defaultHeight(QuestDraftRequest.QuestFlowNode node) {
        if (node != null && "image".equals(node.engine_subtype)) return 180f;
        if (node != null && "video".equals(node.engine_subtype)) return 200f;
        return 72f;
    }

    private int colorFor(String subtype) {
        if ("image".equals(subtype) || "video".equals(subtype)) return Color.parseColor("#4F46E5");
        if ("gesture".equals(subtype) || "text_input".equals(subtype)) return Color.parseColor("#BE123C");
        return Color.parseColor("#1B8A92");
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
