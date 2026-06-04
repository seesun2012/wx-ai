package com.wxai.service;


import com.wxai.memory.MySqlChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiService + @MemoryId + @SystemMessage + MySQL 记忆
 */
@Configuration
@RequiredArgsConstructor
public class TextChatWithMemoryServiceConfig {

    @Resource
    private final OpenAiChatModel openAiChatModel;

    @Resource
    private final MySqlChatMemoryStore mySqlChatMemoryStore;


    @Bean
    public TextChatWithMemoryService textChatWithMemoryService(){

        return AiServices.builder(TextChatWithMemoryService.class)
                .chatModel(openAiChatModel)
                // 关键：每个 userId 自动创建独立记忆
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .id(memoryId)
                                .maxMessages(50)
                                .chatMemoryStore(mySqlChatMemoryStore)
                                .build()
                ).build();

    }

}
