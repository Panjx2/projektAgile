package com.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private Long receiverId;
    private Long projectId;
    private String content;
    private String type;
    private Long fileId;
    private String fileName;
    private String createdAt;
}
