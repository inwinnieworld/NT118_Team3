package com.example.emotiondebugging.model;

public class Emotion {
    private int emotion_id;
    private String emotion_name;
    private String emotion_category; // POSITIVE, NEGATIVE, NEUTRAL
    private int base_weight;
    private String icon_url;
    private String color_hex;

    // Constructors
    public Emotion() {}

    public Emotion(int emotion_id, String emotion_name, String emotion_category) {
        this.emotion_id = emotion_id;
        this.emotion_name = emotion_name;
        this.emotion_category = emotion_category;
    }

    // Getters and Setters
    public int getEmotion_id() {
        return emotion_id;
    }

    public void setEmotion_id(int emotion_id) {
        this.emotion_id = emotion_id;
    }

    public String getEmotion_name() {
        return emotion_name;
    }

    public void setEmotion_name(String emotion_name) {
        this.emotion_name = emotion_name;
    }

    public String getEmotion_category() {
        return emotion_category;
    }

    public void setEmotion_category(String emotion_category) {
        this.emotion_category = emotion_category;
    }

    public int getBase_weight() {
        return base_weight;
    }

    public void setBase_weight(int base_weight) {
        this.base_weight = base_weight;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getColor_hex() {
        return color_hex;
    }

    public void setColor_hex(String color_hex) {
        this.color_hex = color_hex;
    }
}
