package com.wxai.service;

import com.wxai.memory.MySqlChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * 通过service的方式 完成 langchain4j 记忆存储(系统提示词 + mysql+  MessageWindowChatMemory)
 * 实现 从系统提示词 + 对话记忆mysql存储(结构化消息存储)
 */
@Service
@Slf4j
public class TextChatService {

    @Resource
    @Qualifier("openAiChatModel")
    private OpenAiChatModel openAiChatModel;

    @Resource
    private MySqlChatMemoryStore mySqlChatMemoryStore;

    private SystemMessage systemMessage;

    /**
     * 初始化系统提示词
     */
    @PostConstruct
    public void initSystemPrompt() {
        try {
            ClassPathResource resource = new ClassPathResource("system-prompt.txt");// 资源文件路径
            String content = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            systemMessage = SystemMessage.from(content);
            log.info("✅ 系统提示词已加载: {}", content.substring(0, 50) + "...");
        } catch (IOException e) {
            systemMessage = SystemMessage.from("你是一个专业AI助手");
            log.error("系统提示词加载失败", e);
        }

    }


    public String chat(String userId, String message) {
        // 1.加载记忆 每次从 MySQL 加载历史记忆，保证持久化跨请求
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(userId)
                .maxMessages(20)
                .chatMemoryStore(mySqlChatMemoryStore)
                .build();

        // 2.添加当前用户消息（MessageWindowChatMemory 会调用 store.updateMessages 保存）
        UserMessage userMessage = UserMessage.from(message);
        chatMemory.add(userMessage);

        // 3.消息 = 系统提示词 + 历史对话+ 当前对话
        List<ChatMessage> finalMessages = new ArrayList<>();
        finalMessages.add(systemMessage);  // 每次都放在第一位
        finalMessages.addAll(chatMemory.messages()); // 后面跟历史
        // 4. 调用 AI
        ChatResponse response = openAiChatModel.chat(finalMessages);
        AiMessage aiMessage = response.aiMessage();
//        log.info("AI回复: {}", aiMessage.text());
        // 5.将 AI 回复存入记忆
        chatMemory.add(aiMessage);
        return aiMessage.text();
    }

}
