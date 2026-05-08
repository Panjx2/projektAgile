package com.project.model;

public enum TaskStatus {
    TODO("do zrobienia"),
    IN_PROGRESS("w toku"),
    DONE("zrobione");
    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}