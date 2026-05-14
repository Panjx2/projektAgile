package com.example.app.data;

public enum TaskPriority {
    LOW("niski"),
    MEDIUM("średni"),
    HIGH("wysoki");

    private final String displayName;
    TaskPriority(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
