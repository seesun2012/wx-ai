package com.wxai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

//@AiService
public interface TextChatWithMemoryService {

    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(@MemoryId  String userId, @UserMessage String message);
}
