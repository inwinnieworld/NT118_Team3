package com.example.emotiondebugging.model.domain;

public class QuestEngine {
    private String engine_type;
    private String engine_subtype;
    private String symbol;

    public QuestEngine() {
    }

    public QuestEngine(String engineType, String engineSubtype, String symbol) {
        this.engine_type = engineType;
        this.engine_subtype = engineSubtype;
        this.symbol = symbol;
    }

    public String getEngineType() {
        return engine_type;
    }

    public String getEngineSubtype() {
        return engine_subtype;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        String subtype = engine_subtype == null ? "" : engine_subtype.replace("_", " ");
        if (symbol == null || symbol.isEmpty()) return subtype;
        return subtype + " (" + symbol + ")";
    }
}
