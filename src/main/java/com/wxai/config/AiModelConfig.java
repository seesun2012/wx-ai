package com.wxai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * AI 模型配置类
 * 由于 langchain4j spring-boot-starter 不支持多个命名模型，
 * 通过编程式配置声明额外的视觉模型（支持多模态图片输入）
 */
@Configuration
public class AiModelConfig {

    @Value("${ai.model.base-url}")
    private String baseUrl;

    @Value("${ai.model.api-key}")
    private String apiKey;

    @Value("${ai.model.model-text}")
    private String modelText;

    @Value("${ai.model.model-vision}")
    private String modelVision;

    @Value("${ai.model.embedding-model}")
    private String embeddingModel;

    @Value("${ai.model.stream-model}")
    private String streamModel;

    @Resource
    private ChatModelListener chatModelListener;



    @Bean("myStreamChatModel")
    public OpenAiStreamingChatModel myStreamChatModel(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(streamModel)
                .timeout(Duration.ofSeconds(60))
                .temperature(0.7)
                .maxTokens(2048)
                .build();
    }


    @Bean("openAiChatModel")
    public OpenAiChatModel openAiChatModel(){
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelText)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(2)
                .listeners(List.of(chatModelListener))
                .build();
    }

    // ... existing code ...

    @Bean("visionChatModel")
    public OpenAiChatModel visionChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelVision)
                .temperature(0.1)
                .maxTokens(2048)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(2)
                .build();
    }


    /**
     *  硅基流动 Embedding 向量模型
     *  maxSegmentsPerBatch(32)：硅基流动单次 batch 上限为 32，超出会报 input batch size > maximum allowed batch size
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(embeddingModel)
                .maxSegmentsPerBatch(10)  // 硅基流动 API 单批次最大 32 条
                .build();
    }

    /**
     * 内存向量存储（用于 RAG 检索）
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

}
