package com.wxai.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiChatMessage {
    private Long id;
    private String userId;
    private String memoryId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}