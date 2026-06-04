package com.wxai.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiChatMemory {

    private Long id;
    private String userId;
    private String memoryId;
    private String title;
    private String messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}