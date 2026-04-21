package com.example.emotiondebugging.ui.gitgraph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.emotiondebugging.model.GitGraphCommit;
import com.example.emotiondebugging.model.GitGraphMerge;
import com.example.emotiondebugging.utils.QuestColorGenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Custom View to draw Git Graph
 * Displays commits and merges in a git-like visualization
 */
public class GitGraphView extends View {
    
    // Layout constants
    private static final float MAIN_BRANCH_X_RATIO = 0.3f; // Main branch at 30% from left
    private static final float QUEST_BRANCH_OFFSET = 200f; // Quest branches offset to right
    private static final float NODE_SPACING = 150f; // Vertical spacing between nodes
    private static final float NODE_RADIUS = 20f;
    private static final float MERGE_NODE_RADIUS = 30f;
    private static final float STROKE_WIDTH = 6f;
    
    // Paints
    private Paint mainBranchPaint;
    private Paint questBranchPaint;
    private Paint nodePaint;
    private Paint textPaint;
    private Paint mergeLabelPaint;
    
    // Data
    private List<GitGraphNode> nodes = new ArrayList<>();
    private Map<Integer, List<GitGraphNode>> questBranches = new HashMap<>();
    
    // Zoom and Pan
    private float scaleFactor = 1.0f;
    private float translateX = 0f;
    private float translateY = 0f;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    
    // Touch handling
    private OnNodeClickListener nodeClickListener;
    
    public interface OnNodeClickListener {
        void onNodeClick(GitGraphNode node, float x, float y);
    }
    
    public GitGraphView(Context context) {
        super(context);
        init();
    }
    
    public GitGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Initialize paints
        mainBranchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainBranchPaint.setColor(QuestColorGenerator.getMainBranchColor());
        mainBranchPaint.setStrokeWidth(STROKE_WIDTH);
        mainBranchPaint.setStyle(Paint.Style.STROKE);
        
        questBranchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        questBranchPaint.setStrokeWidth(STROKE_WIDTH);
        questBranchPaint.setStyle(Paint.Style.STROKE);
        
        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodePaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        mergeLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mergeLabelPaint.setColor(Color.parseColor("#9CA3AF"));
        mergeLabelPaint.setTextSize(28f);
        
        // Initialize gesture detectors
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
    }
    
    /**
     * Set data for the graph
     */
    public void setData(List<GitGraphCommit> commits, List<GitGraphMerge> merges) {
        android.util.Log.d("GitGraphView", "setData called with " + commits.size() + " commits, " + merges.size() + " merges");
        
        nodes.clear();
        questBranches.clear();
        
        // Combine commits and merges, sort by timestamp
        List<Object> allItems = new ArrayList<>();
        allItems.addAll(commits);
        allItems.addAll(merges);
        
        android.util.Log.d("GitGraphView", "Total items to process: " + allItems.size());
        
        Collections.sort(allItems, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String time1 = getTimestamp(o1);
                String time2 = getTimestamp(o2);
                return time1.compareTo(time2);
            }
        });
        
        // Build nodes
        for (Object item : allItems) {
            if (item instanceof GitGraphCommit) {
                GitGraphCommit commit = (GitGraphCommit) item;
                android.util.Log.d("GitGraphView", "Adding commit: " + commit.getMessage() + " [" + commit.getBranch_type() + "]");
                nodes.add(GitGraphNode.fromCommit(commit));
            } else if (item instanceof GitGraphMerge) {
                android.util.Log.d("GitGraphView", "Adding merge: " + ((GitGraphMerge) item).getMerge_date());
                nodes.add(GitGraphNode.fromMerge((GitGraphMerge) item));
            }
        }
        
        android.util.Log.d("GitGraphView", "Total nodes created: " + nodes.size());
        
        // Group quest branches
        for (GitGraphNode node : nodes) {
            if (node.isQuestBranch()) {
                int questId = node.getQuestId();
                if (!questBranches.containsKey(questId)) {
                    questBranches.put(questId, new ArrayList<>());
                }
                questBranches.get(questId).add(node);
            }
        }
        
        android.util.Log.d("GitGraphView", "Quest branches: " + questBranches.size());
        
        // Mark quest start/end
        for (List<GitGraphNode> questNodes : questBranches.values()) {
            if (!questNodes.isEmpty()) {
                questNodes.get(0).setQuestStart(true);
                questNodes.get(questNodes.size() - 1).setQuestEnd(true);
            }
        }
        
        // Calculate positions
        calculateNodePositions();
        
        invalidate();
    }
    
    private String getTimestamp(Object item) {
        if (item instanceof GitGraphCommit) {
            return ((GitGraphCommit) item).getCreated_at();
        } else if (item instanceof GitGraphMerge) {
            return ((GitGraphMerge) item).getCreated_at();
        }
        return "";
    }
    
    /**
     * Calculate X,Y positions for all nodes
     */
    private void calculateNodePositions() {
        // Use fixed width or measured width (fallback to 1080 if not measured yet)
        int viewWidth = getWidth() > 0 ? getWidth() : 1080;
        float mainBranchX = viewWidth * MAIN_BRANCH_X_RATIO;
        float currentY = 100f; // Start from top with padding
        
        Map<Integer, Integer> questIndexMap = new HashMap<>();
        int questCounter = 0;
        
        // Group nodes by date for proper positioning
        String currentDate = "";
        
        for (GitGraphNode node : nodes) {
            String nodeDate = extractDate(node.getTimestamp());
            
            // Add extra spacing between different dates
            if (!nodeDate.equals(currentDate) && !currentDate.isEmpty()) {
                currentY += NODE_SPACING * 0.5f; // Extra spacing between days
            }
            currentDate = nodeDate;
            
            if (node.isMergeNode()) {
                // Merge nodes always on main branch line
                node.setX(mainBranchX);
                node.setY(currentY);
                node.setColor(QuestColorGenerator.getMergeNodeColor());
                node.setRadius(MERGE_NODE_RADIUS);
                
            } else if (node.isMainBranch()) {
                // Main branch nodes on the main line
                node.setX(mainBranchX);
                node.setY(currentY);
                node.setColor(QuestColorGenerator.getMainBranchColor());
                node.setRadius(NODE_RADIUS);
                
            } else if (node.isQuestBranch()) {
                // Quest branch nodes offset to the right
                int questId = node.getQuestId();
                
                // Assign quest index if not exists
                if (!questIndexMap.containsKey(questId)) {
                    questIndexMap.put(questId, questCounter++);
                }
                
                int questIndex = questIndexMap.get(questId);
                float questX = mainBranchX + QUEST_BRANCH_OFFSET + (questIndex * 100f);
                
                node.setX(questX);
                node.setY(currentY);
                node.setColor(QuestColorGenerator.getColorForQuest(questId));
                node.setRadius(NODE_RADIUS);
            }
            
            currentY += NODE_SPACING;
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (nodes.isEmpty()) {
            return;
        }
        
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);
        
        // Draw connections first (behind nodes)
        drawConnections(canvas);
        
        // Draw nodes
        drawNodes(canvas);
        
        // Draw merge labels
        drawMergeLabels(canvas);
        
        canvas.restore();
    }
    
    private void drawConnections(Canvas canvas) {
        int viewWidth = getWidth() > 0 ? getWidth() : 1080;
        float mainBranchX = viewWidth * MAIN_BRANCH_X_RATIO;
        
        // Draw main branch line (continuous vertical line)
        if (nodes.size() > 1) {
            float startY = nodes.get(0).getY();
            float endY = nodes.get(nodes.size() - 1).getY();
            canvas.drawLine(mainBranchX, startY, mainBranchX, endY, mainBranchPaint);
        }
        
        // Draw quest branch connections
        for (Map.Entry<Integer, List<GitGraphNode>> entry : questBranches.entrySet()) {
            int questId = entry.getKey();
            List<GitGraphNode> questNodes = entry.getValue();
            
            if (questNodes.isEmpty()) continue;
            
            questBranchPaint.setColor(QuestColorGenerator.getColorForQuest(questId));
            
            GitGraphNode questStart = questNodes.get(0);
            GitGraphNode questEnd = questNodes.get(questNodes.size() - 1);
            
            // Find parent main node (node before quest start)
            int startIndex = nodes.indexOf(questStart);
            GitGraphNode parentNode = null;
            
            for (int i = startIndex - 1; i >= 0; i--) {
                if (nodes.get(i).isMainBranch()) {
                    parentNode = nodes.get(i);
                    break;
                }
            }
            
            // Draw branch out curve from main to quest start
            if (parentNode != null) {
                drawBezierCurve(canvas, 
                        parentNode.getX(), parentNode.getY(),
                        questStart.getX(), questStart.getY(),
                        questBranchPaint);
            }
            
            // Draw connections between quest nodes (vertical line for quest branch)
            for (int i = 0; i < questNodes.size() - 1; i++) {
                GitGraphNode from = questNodes.get(i);
                GitGraphNode to = questNodes.get(i + 1);
                canvas.drawLine(from.getX(), from.getY(), to.getX(), to.getY(), questBranchPaint);
            }
            
            // Check if this quest has been merged (look for merge node after quest end)
            int endIndex = nodes.indexOf(questEnd);
            GitGraphNode mergeNode = null;
            
            // Find the next merge node that belongs to the same date as quest end
            String questEndDate = extractDate(questEnd.getTimestamp());
            
            for (int i = endIndex + 1; i < nodes.size(); i++) {
                GitGraphNode node = nodes.get(i);
                if (node.isMergeNode()) {
                    String mergeDate = node.getMerge().getMerge_date();
                    // Check if merge date matches quest end date (merge happens at end of day)
                    if (mergeDate.equals(questEndDate)) {
                        mergeNode = node;
                        break;
                    }
                }
            }
            
            // Only draw merge curve if merge node exists
            if (mergeNode != null) {
                drawBezierCurve(canvas,
                        questEnd.getX(), questEnd.getY(),
                        mergeNode.getX(), mergeNode.getY(),
                        questBranchPaint);
            }
            // If no merge node, quest branch stays parallel (not merged yet)
        }
    }
    
    /**
     * Extract date (YYYY-MM-DD) from timestamp
     */
    private String extractDate(String timestamp) {
        if (timestamp == null || timestamp.length() < 10) {
            return "";
        }
        return timestamp.substring(0, 10);
    }
    
    private void drawBezierCurve(Canvas canvas, float x1, float y1, float x2, float y2, Paint paint) {
        Path path = new Path();
        path.moveTo(x1, y1);
        
        // Control points for smooth curve
        float controlX1 = x1 + (x2 - x1) * 0.5f;
        float controlY1 = y1;
        float controlX2 = x1 + (x2 - x1) * 0.5f;
        float controlY2 = y2;
        
        path.cubicTo(controlX1, controlY1, controlX2, controlY2, x2, y2);
        canvas.drawPath(path, paint);
    }
    
    private void drawNodes(Canvas canvas) {
        for (GitGraphNode node : nodes) {
            nodePaint.setColor(node.getColor());
            canvas.drawCircle(node.getX(), node.getY(), node.getRadius(), nodePaint);
        }
    }
    
    private void drawMergeLabels(Canvas canvas) {
        for (GitGraphNode node : nodes) {
            if (node.isMergeNode()) {
                GitGraphMerge merge = node.getMerge();
                
                // Build merge label with quest info
                StringBuilder labelBuilder = new StringBuilder("Tổng kết\n");
                labelBuilder.append(formatDate(merge.getMerge_date()));
                
                // Find which quests were merged on this date
                String mergeDate = merge.getMerge_date();
                List<Integer> mergedQuests = new ArrayList<>();
                
                for (GitGraphNode commitNode : nodes) {
                    if (commitNode.isQuestBranch()) {
                        String commitDate = extractDate(commitNode.getTimestamp());
                        if (commitDate.equals(mergeDate)) {
                            int questId = commitNode.getQuestId();
                            if (!mergedQuests.contains(questId)) {
                                mergedQuests.add(questId);
                            }
                        }
                    }
                }
                
                // Add "Merged from main and Quest #X, Quest #Y"
                if (!mergedQuests.isEmpty()) {
                    labelBuilder.append("\nMerged from main");
                    for (int questId : mergedQuests) {
                        labelBuilder.append("\nand Quest #").append(questId);
                    }
                }
                
                String label = labelBuilder.toString();
                
                // Draw label to the left of merge node
                float labelX = node.getX() - 150f;
                float labelY = node.getY();
                
                // Calculate background size based on text
                String[] lines = label.split("\n");
                float maxWidth = 0;
                for (String line : lines) {
                    float width = mergeLabelPaint.measureText(line);
                    if (width > maxWidth) maxWidth = width;
                }
                
                float bgWidth = maxWidth + 20;
                float bgHeight = lines.length * 35 + 10;
                
                // Draw background
                RectF bgRect = new RectF(
                        labelX - bgWidth/2, 
                        labelY - bgHeight/2, 
                        labelX + bgWidth/2, 
                        labelY + bgHeight/2
                );
                Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bgPaint.setColor(Color.parseColor("#374151"));
                bgPaint.setStyle(Paint.Style.FILL);
                canvas.drawRoundRect(bgRect, 10, 10, bgPaint);
                
                // Draw text
                float textY = labelY - (lines.length - 1) * 17.5f;
                for (String line : lines) {
                    canvas.drawText(line, labelX, textY, mergeLabelPaint);
                    textY += 35;
                }
            }
        }
    }
    
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }
    
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
            invalidate();
            return true;
        }
    }
    
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            translateX -= distanceX;
            translateY -= distanceY;
            invalidate();
            return true;
        }
        
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Check if tap is on a node
            float touchX = (e.getX() - translateX) / scaleFactor;
            float touchY = (e.getY() - translateY) / scaleFactor;
            
            for (GitGraphNode node : nodes) {
                float dx = touchX - node.getX();
                float dy = touchY - node.getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance <= node.getRadius() + 20) {
                    if (nodeClickListener != null) {
                        nodeClickListener.onNodeClick(node, e.getX(), e.getY());
                    }
                    return true;
                }
            }
            
            return false;
        }
    }
    
    public void setOnNodeClickListener(OnNodeClickListener listener) {
        this.nodeClickListener = listener;
    }
    
    public void zoomIn() {
        scaleFactor = Math.min(scaleFactor * 1.2f, 3.0f);
        invalidate();
    }
    
    public void zoomOut() {
        scaleFactor = Math.max(scaleFactor / 1.2f, 0.5f);
        invalidate();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        // Calculate required height based on number of nodes
        int requiredHeight = (int) (nodes.size() * NODE_SPACING + 200);
        setMeasuredDimension(getMeasuredWidth(), Math.max(getMeasuredHeight(), requiredHeight));
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Recalculate positions when view size changes
        if (!nodes.isEmpty() && w > 0) {
            calculateNodePositions();
            invalidate();
        }
    }
}
