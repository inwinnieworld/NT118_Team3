package com.example.emotiondebugging.utils;

import android.graphics.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Generate consistent colors for quest branches
 * Each quest_id gets a unique color that remains consistent
 */
public class QuestColorGenerator {
    
    private static final Map<Integer, Integer> questColorCache = new HashMap<>();
    
    // Predefined vibrant colors for quest branches (excluding cyan for main)
    private static final int[] QUEST_COLORS = {
            Color.parseColor("#FCD34D"), // Yellow
            Color.parseColor("#F472B6"), // Pink
            Color.parseColor("#A78BFA"), // Purple
            Color.parseColor("#FB923C"), // Orange
            Color.parseColor("#34D399"), // Green
            Color.parseColor("#60A5FA"), // Blue
            Color.parseColor("#F87171"), // Red
            Color.parseColor("#FBBF24"), // Amber
            Color.parseColor("#C084FC"), // Violet
            Color.parseColor("#4ADE80"), // Lime
    };
    
    /**
     * Get color for a quest branch
     * Same quest_id always returns same color
     */
    public static int getColorForQuest(int questId) {
        if (questColorCache.containsKey(questId)) {
            return questColorCache.get(questId);
        }
        
        // Use quest_id as seed for consistent color selection
        int colorIndex = Math.abs(questId) % QUEST_COLORS.length;
        int color = QUEST_COLORS[colorIndex];
        
        questColorCache.put(questId, color);
        return color;
    }
    
    /**
     * Get color for main branch (always cyan)
     */
    public static int getMainBranchColor() {
        return Color.parseColor("#06B6D4"); // Cyan
    }
    
    /**
     * Get color for merge node (always purple)
     */
    public static int getMergeNodeColor() {
        return Color.parseColor("#9333EA"); // Purple
    }
    
    /**
     * Clear cache (useful for testing)
     */
    public static void clearCache() {
        questColorCache.clear();
    }
}
