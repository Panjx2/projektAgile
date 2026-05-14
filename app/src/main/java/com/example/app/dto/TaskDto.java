package com.example.app.dto;

import com.example.app.data.TaskPriority;
import com.example.app.data.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TaskDto {

    @JsonProperty("task_id")
    private Long taskId;

    private String name;
    private TaskPriority priority;
    private TaskStatus status;

    private Long assignedUserId;
    private Long projectId;
}