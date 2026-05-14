package com.project.model;

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
