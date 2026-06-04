package com.wxai.ai;

import com.wxai.ai.tool.InterviewQuestionTool;
import com.wxai.memory.MySqlChatMemoryStore;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AiCodeServiceFactory {

    private final OpenAiChatModel openAiChatModel;
    private final ContentRetriever contentRetriever;
    private final McpToolProvider mcpToolProvider;
    private final OpenAiStreamingChatModel myStreamChatModel;
    private final MySqlChatMemoryStore mySqlChatMemoryStore;

    @Bean
    public AiCodeService aiCodeHelperService() {
        return AiServices.builder(AiCodeService.class)
                .chatModel(openAiChatModel)
                .streamingChatModel(myStreamChatModel)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .maxMessages(10)
                                .chatMemoryStore(mySqlChatMemoryStore)
                                .build())
                .contentRetriever(contentRetriever)
                .tools(new InterviewQuestionTool())
                .toolProvider(mcpToolProvider)
                .build();
    }
}