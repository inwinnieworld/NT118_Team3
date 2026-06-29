package com.example.emotiondebugging.ui.staff;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.emotiondebugging.model.request.QuestDraftRequest;

import java.util.ArrayList;
import java.util.List;

public class QuestCanvasView extends View {

    public interface CanvasListener {
        void onNodeSelected(String nodeId);
        void onCanvasSelected();
        void onNodeMoved(String nodeId, float x, float y);
        void onSequentialEdgeCreated(String sourceNodeId, String targetNodeId);
        void onEdgeSelected(String edgeId);
    }

    private final Paint pagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint smallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint chipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgeLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<QuestDraftRequest.QuestFlowNode> nodes = new ArrayList<>();
    private List<QuestDraftRequest.QuestFlowNode> countNodes = new ArrayList<>();
    private List<QuestDraftRequest.QuestFlowEdge> edges = new ArrayList<>();
    private String selectedNodeId;
    private String selectedEdgeId;
    private boolean connectMode;
    private String connectModeType = "sequential";
    private String draggingNodeId;
    private String connectSourceNodeId;
    private float dragOffsetX;
    private float dragOffsetY;
    private float pointerX;
    private float pointerY;
    private CanvasListener listener;
    private String backgroundColor = "#FFFFFF";
    private String backgroundUrl = "";

    public QuestCanvasView(Context context) {
        super(context);
        init();
    }

    public QuestCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        pagePaint.setStyle(Paint.Style.FILL);
        gridPaint.setColor(Color.parseColor("#EEF2F7"));
        gridPaint.setStrokeWidth(1f);
        nodePaint.setStyle(Paint.Style.FILL);
        selectedPaint.setColor(Color.parseColor("#14B8A6"));
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(dp(3));
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dp(14));
        smallTextPaint.setColor(Color.WHITE);
        smallTextPaint.setTextSize(dp(10));
        chipPaint.setStyle(Paint.Style.FILL);
        edgePaint.setColor(Color.parseColor("#334155"));
        edgePaint.setStrokeWidth(dp(2));
        edgePaint.setStyle(Paint.Style.STROKE);
        edgeLabelPaint.setColor(Color.parseColor("#334155"));
        edgeLabelPaint.setTextSize(dp(10));
        edgeLabelPaint.setFakeBoldText(true);
        hintPaint.setColor(Color.parseColor("#64748B"));
        hintPaint.setTextSize(dp(14));
        setFocusable(true);
    }

    public void setCanvasListener(CanvasListener listener) {
        this.listener = listener;
    }

    public void setData(List<QuestDraftRequest.QuestFlowNode> nodes,
                        List<QuestDraftRequest.QuestFlowEdge> edges,
                        String selectedNodeId,
                        String selectedEdgeId,
                        boolean connectMode,
                        String connectModeType) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
        this.countNodes = this.nodes;
        this.edges = edges == null ? new ArrayList<>() : edges;
        this.selectedNodeId = selectedNodeId;
        this.selectedEdgeId = selectedEdgeId;
        this.connectMode = connectMode;
        this.connectModeType = connectModeType == null ? "sequential" : connectModeType;
        invalidate();
    }

    public void setCountNodes(List<QuestDraftRequest.QuestFlowNode> nodes) {
        this.countNodes = nodes == null ? new ArrayList<>() : nodes;
        invalidate();
    }

    public void setBackgroundConfig(String backgroundColor, String backgroundUrl) {
        this.backgroundColor = backgroundColor == null || backgroundColor.trim().isEmpty()
                ? "#FFFFFF" : backgroundColor.trim();
        this.backgroundUrl = backgroundUrl == null ? "" : backgroundUrl.trim();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPage(canvas);
        drawEdges(canvas);
        drawNodes(canvas);
        drawConnectPreview(canvas);
        drawEmptyHint(canvas);
    }

    private void drawPage(Canvas canvas) {
        int color = Color.WHITE;
        try {
            color = Color.parseColor(backgroundColor);
        } catch (Exception ignored) {
        }
        pagePaint.setColor(color);
        canvas.drawRect(0, 0, getWidth(), getHeight(), pagePaint);

        for (int x = 0; x < getWidth(); x += dp(32)) {
            canvas.drawLine(x, 0, x, getHeight(), gridPaint);
        }
        for (int y = 0; y < getHeight(); y += dp(32)) {
            canvas.drawLine(0, y, getWidth(), y, gridPaint);
        }

        if (!backgroundUrl.isEmpty()) {
            hintPaint.setColor(Color.parseColor("#64748B"));
            canvas.drawText("Background selected", dp(16), dp(24), hintPaint);
        }
    }

    private void drawEdges(Canvas canvas) {
        for (QuestDraftRequest.QuestFlowEdge edge : edges) {
            QuestDraftRequest.QuestFlowNode source = findNode(edge.source_client_node_id);
            QuestDraftRequest.QuestFlowNode target = findNode(edge.target_client_node_id);
            if (source == null || target == null) continue;

            RectF from = rectFor(source);
            RectF to = rectFor(target);
            edgePaint.setColor(edge.client_edge_id != null && edge.client_edge_id.equals(selectedEdgeId)
                    ? Color.parseColor("#0D9488") : Color.parseColor("#334155"));
            edgePaint.setStrokeWidth(edge.client_edge_id != null && edge.client_edge_id.equals(selectedEdgeId)
                    ? dp(3) : dp(2));
            if ("parallel".equals(edge.flow_type)) {
                drawParallelLink(canvas, from.right, from.centerY(), to.left, to.centerY());
                drawParallelEndpointLabel(canvas, "A", from.right + dp(8), from.centerY() - dp(10));
                drawParallelEndpointLabel(canvas, "B", to.left - dp(22), to.centerY() - dp(10));
            } else {
                drawArrow(canvas, from.right, from.centerY(), to.left, to.centerY());
            }
            float labelX = (from.right + to.left) / 2f;
            float labelY = (from.centerY() + to.centerY()) / 2f - dp(8);
            canvas.drawText(edgeLabel(edge, source), labelX, labelY, edgeLabelPaint);
        }
    }

    private String edgeLabel(QuestDraftRequest.QuestFlowEdge edge,
                             QuestDraftRequest.QuestFlowNode source) {
        if ("parallel".equals(edge.flow_type)) {
            if (edge.config != null && edge.config.get("completion_condition") != null) {
                return String.valueOf(edge.config.get("completion_condition"));
            }
            if (edge.completion_condition != null) return edge.completion_condition;
            return "A_OR_B";
        }
        String type = edge.config != null && edge.config.get("transition_type") != null
                ? String.valueOf(edge.config.get("transition_type")) : "delay";
        String trigger = "immediate".equals(type) ? "now" : edgeDelaySeconds(edge) + "s";
        String effect = edge.config != null && edge.config.get("transition_effect") != null
                ? String.valueOf(edge.config.get("transition_effect")) : "fade";
        return "none".equals(effect) ? trigger : trigger + " | " + effect.replace('_', ' ');
    }

    private int edgeDelaySeconds(QuestDraftRequest.QuestFlowEdge edge) {
        if (edge.config == null || edge.config.get("delay_seconds") == null) return 3;
        try {
            Object value = edge.config.get("delay_seconds");
            return value instanceof Number
                    ? ((Number) value).intValue()
                    : Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return 3;
        }
    }

    private void drawNodes(Canvas canvas) {
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            if (!"composite".equals(node.engine_subtype)) continue;
            RectF rect = rectFor(node);
            nodePaint.setColor(Color.parseColor("#F1F5F9"));
            canvas.drawRoundRect(rect, dp(8), dp(8), nodePaint);
            edgePaint.setColor(Color.parseColor("#64748B"));
            edgePaint.setStrokeWidth(dp(2));
            canvas.drawRoundRect(rect, dp(8), dp(8), edgePaint);
            edgePaint.setColor(Color.parseColor("#334155"));
            hintPaint.setColor(Color.parseColor("#334155"));
            hintPaint.setFakeBoldText(true);
            String frameName = node.display_name == null ? "Frame" : node.display_name;
            canvas.drawText("FRAME  " + frameName, rect.left + dp(14), rect.top + dp(24), hintPaint);
            int childCount = 0;
            for (QuestDraftRequest.QuestFlowNode child : countNodes) {
                if (node.client_node_id.equals(child.parent_client_node_id)) childCount++;
            }
            hintPaint.setColor(Color.parseColor("#64748B"));
            hintPaint.setTextSize(dp(11));
            canvas.drawText(childCount == 0 ? "Drop engines here" : childCount + " engine(s) inside",
                    rect.left + dp(14), rect.top + dp(46), hintPaint);
            hintPaint.setTextSize(dp(14));
            hintPaint.setFakeBoldText(false);
            if (selectedNodeId != null && selectedNodeId.equals(node.client_node_id)) {
                canvas.drawRoundRect(rect, dp(8), dp(8), selectedPaint);
            }
        }

        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            if ("composite".equals(node.engine_subtype)) continue;
            RectF rect = rectFor(node);
            int nodeColor = colorForNode(node.engine_subtype);

            nodePaint.setColor(nodeColor);
            canvas.drawRoundRect(rect, dp(10), dp(10), nodePaint);

            if (selectedNodeId != null && selectedNodeId.equals(node.client_node_id)) {
                canvas.drawRoundRect(rect, dp(10), dp(10), selectedPaint);
            }

            chipPaint.setColor(darken(nodeColor));
            RectF chip = new RectF(rect.left + dp(10), rect.top + dp(48), rect.left + dp(60), rect.top + dp(70));
            canvas.drawRoundRect(chip, dp(11), dp(11), chipPaint);

            smallTextPaint.setFakeBoldText(true);
            canvas.drawText(symbolFor(node.engine_subtype), chip.left + dp(8), chip.top + dp(15), smallTextPaint);

            smallTextPaint.setFakeBoldText(false);
            canvas.drawText(labelFor(node.engine_subtype), rect.left + dp(70), rect.top + dp(63), smallTextPaint);

            textPaint.setFakeBoldText(true);
            String name = node.display_name == null || node.display_name.trim().isEmpty()
                    ? labelFor(node.engine_subtype) : node.display_name;
            canvas.drawText(trimToWidth(name, rect.width() - dp(20), textPaint), rect.left + dp(10), rect.top + dp(30), textPaint);
        }
    }

    private void drawConnectPreview(Canvas canvas) {
        if (!connectMode || connectSourceNodeId == null) return;
        QuestDraftRequest.QuestFlowNode source = findNode(connectSourceNodeId);
        if (source == null) return;
        RectF rect = rectFor(source);
        if ("parallel".equals(connectModeType)) {
            drawParallelLink(canvas, rect.right, rect.centerY(), pointerX, pointerY);
        } else {
            drawArrow(canvas, rect.right, rect.centerY(), pointerX, pointerY);
        }
    }

    private void drawEmptyHint(Canvas canvas) {
        if (!nodes.isEmpty()) return;
        hintPaint.setColor(Color.parseColor("#64748B"));
        canvas.drawText("Drag engines from the left toolbox onto this canvas.", dp(24), getHeight() / 2f, hintPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pointerX = event.getX();
        pointerY = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                QuestDraftRequest.QuestFlowNode hit = hitTest(pointerX, pointerY);
                if (hit == null) {
                    QuestDraftRequest.QuestFlowEdge edge = edgeHitTest(pointerX, pointerY);
                    if (!connectMode && edge != null && listener != null) {
                        listener.onEdgeSelected(edge.client_edge_id);
                        return true;
                    }
                    if (listener != null) listener.onCanvasSelected();
                    invalidate();
                    return true;
                }

                if (connectMode) {
                    connectSourceNodeId = hit.client_node_id;
                } else {
                    draggingNodeId = hit.client_node_id;
                    RectF rect = rectFor(hit);
                    dragOffsetX = pointerX - rect.left;
                    dragOffsetY = pointerY - rect.top;
                }
                if (listener != null) listener.onNodeSelected(hit.client_node_id);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!connectMode && draggingNodeId != null && listener != null) {
                    listener.onNodeMoved(draggingNodeId, pointerX - dragOffsetX, pointerY - dragOffsetY);
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (connectMode && connectSourceNodeId != null) {
                    QuestDraftRequest.QuestFlowNode target = hitTest(pointerX, pointerY);
                    if (target != null && listener != null) {
                        listener.onSequentialEdgeCreated(connectSourceNodeId, target.client_node_id);
                    }
                    connectSourceNodeId = null;
                }
                draggingNodeId = null;
                invalidate();
                return true;
            default:
                return true;
        }
    }

    private QuestDraftRequest.QuestFlowNode hitTest(float x, float y) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            QuestDraftRequest.QuestFlowNode node = nodes.get(i);
            if (rectFor(node).contains(x, y)) return node;
        }
        return null;
    }

    private QuestDraftRequest.QuestFlowNode findNode(String nodeId) {
        for (QuestDraftRequest.QuestFlowNode node : nodes) {
            if (node.client_node_id.equals(nodeId)) return node;
        }
        return null;
    }

    private QuestDraftRequest.QuestFlowEdge edgeHitTest(float x, float y) {
        float tolerance = dp(14);
        for (int edgeIndex = edges.size() - 1; edgeIndex >= 0; edgeIndex--) {
            QuestDraftRequest.QuestFlowEdge edge = edges.get(edgeIndex);
            QuestDraftRequest.QuestFlowNode source = findNode(edge.source_client_node_id);
            QuestDraftRequest.QuestFlowNode target = findNode(edge.target_client_node_id);
            if (source == null || target == null) continue;

            RectF from = rectFor(source);
            RectF to = rectFor(target);
            float startX = from.right;
            float startY = from.centerY();
            float endX = to.left;
            float endY = to.centerY();
            float midX = (startX + endX) / 2f;
            float previousX = startX;
            float previousY = startY;

            for (int step = 1; step <= 20; step++) {
                float t = step / 20f;
                float oneMinusT = 1f - t;
                float curveX = oneMinusT * oneMinusT * oneMinusT * startX
                        + 3f * oneMinusT * oneMinusT * t * midX
                        + 3f * oneMinusT * t * t * midX
                        + t * t * t * endX;
                float curveY = oneMinusT * oneMinusT * oneMinusT * startY
                        + 3f * oneMinusT * oneMinusT * t * startY
                        + 3f * oneMinusT * t * t * endY
                        + t * t * t * endY;
                if (distanceToSegment(x, y, previousX, previousY, curveX, curveY) <= tolerance) {
                    return edge;
                }
                previousX = curveX;
                previousY = curveY;
            }
        }
        return null;
    }

    private float distanceToSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        if (dx == 0f && dy == 0f) return (float) Math.hypot(px - x1, py - y1);
        float t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0f, Math.min(1f, t));
        return (float) Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }

    private RectF rectFor(QuestDraftRequest.QuestFlowNode node) {
        float w = node.width == null ? dp(190) : node.width.floatValue();
        float h = node.height == null ? dp(82) : node.height.floatValue();
        return new RectF((float) node.position_x, (float) node.position_y,
                (float) node.position_x + w, (float) node.position_y + h);
    }

    private void drawArrow(Canvas canvas, float startX, float startY, float endX, float endY) {
        Path path = new Path();
        float midX = (startX + endX) / 2f;
        path.moveTo(startX, startY);
        path.cubicTo(midX, startY, midX, endY, endX, endY);
        canvas.drawPath(path, edgePaint);

        double angle = Math.atan2(endY - startY, endX - startX);
        float size = dp(8);
        Path head = new Path();
        head.moveTo(endX, endY);
        head.lineTo((float) (endX - size * Math.cos(angle - Math.PI / 6)),
                (float) (endY - size * Math.sin(angle - Math.PI / 6)));
        head.moveTo(endX, endY);
        head.lineTo((float) (endX - size * Math.cos(angle + Math.PI / 6)),
                (float) (endY - size * Math.sin(angle + Math.PI / 6)));
        canvas.drawPath(head, edgePaint);
    }

    private void drawParallelLink(Canvas canvas, float startX, float startY, float endX, float endY) {
        Path path = new Path();
        float midX = (startX + endX) / 2f;
        path.moveTo(startX, startY);
        path.cubicTo(midX, startY, midX, endY, endX, endY);
        canvas.drawPath(path, edgePaint);

        float labelX = midX;
        float labelY = (startY + endY) / 2f;
        Paint.Style oldStyle = edgePaint.getStyle();
        edgePaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(
                new RectF(labelX - dp(16), labelY - dp(13), labelX + dp(16), labelY + dp(13)),
                dp(8), dp(8), edgePaint
        );
        textPaint.setTextSize(dp(18));
        textPaint.setFakeBoldText(true);
        canvas.drawText("=", labelX - dp(5), labelY + dp(6), textPaint);
        textPaint.setTextSize(dp(14));
        edgePaint.setStyle(oldStyle);
    }

    private void drawParallelEndpointLabel(Canvas canvas, String label, float x, float y) {
        Paint.Style oldStyle = edgePaint.getStyle();
        int oldColor = edgePaint.getColor();
        edgePaint.setStyle(Paint.Style.FILL);
        edgePaint.setColor(Color.parseColor("#0F766E"));
        canvas.drawOval(new RectF(x, y, x + dp(18), y + dp(18)), edgePaint);
        smallTextPaint.setColor(Color.WHITE);
        smallTextPaint.setFakeBoldText(true);
        canvas.drawText(label, x + dp(6), y + dp(13), smallTextPaint);
        smallTextPaint.setFakeBoldText(false);
        smallTextPaint.setColor(Color.WHITE);
        edgePaint.setColor(oldColor);
        edgePaint.setStyle(oldStyle);
    }

    private int colorForNode(String subtype) {
        if ("text".equals(subtype) || "timer".equals(subtype)) return Color.parseColor("#1B8A92");
        if ("image".equals(subtype) || "video".equals(subtype) || "audio".equals(subtype)) return Color.parseColor("#4F46E5");
        if ("gesture".equals(subtype) || "sensor".equals(subtype) || "voice".equals(subtype) || "text_input".equals(subtype)) return Color.parseColor("#BE123C");
        if ("quest".equals(subtype)) return Color.parseColor("#7C3AED");
        return Color.parseColor("#334155");
    }

    private String symbolFor(String subtype) {
        switch (subtype) {
            case "sequential": return "->";
            case "parallel": return "||";
            case "composite": return "[]";
            case "quest": return "Q";
            case "image": return "IMG";
            case "video": return "VID";
            case "audio": return "AUD";
            case "gesture": return "TAP";
            case "sensor": return "SNS";
            case "voice": return "MIC";
            case "text_input": return "IN";
            case "text": return "TXT";
            case "timer": return "60";
            default: return "?";
        }
    }

    private String labelFor(String subtype) {
        if ("text_input".equals(subtype)) return "TEXT INPUT";
        if ("composite".equals(subtype)) return "FRAME";
        if (subtype == null) return "ENGINE";
        return subtype.replace("_", " ").toUpperCase();
    }

    private String trimToWidth(String value, float maxWidth, Paint paint) {
        if (value == null) return "";
        if (paint.measureText(value) <= maxWidth) return value;
        String ellipsis = "...";
        String current = value;
        while (current.length() > 1 && paint.measureText(current + ellipsis) > maxWidth) {
            current = current.substring(0, current.length() - 1);
        }
        return current + ellipsis;
    }

    private int darken(int color) {
        int r = Math.max(0, (int) (Color.red(color) * 0.72f));
        int g = Math.max(0, (int) (Color.green(color) * 0.72f));
        int b = Math.max(0, (int) (Color.blue(color) * 0.72f));
        return Color.rgb(r, g, b);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
