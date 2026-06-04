package com.wxai.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiCode {

    /** 文本对话模型（由 langchain4j starter 自动注入） */
    @Resource
    @Qualifier("openAiChatModel")
    private OpenAiChatModel openAiChatModel;

    /** 多模态视觉模型（支持图片 + 文本，由 AiModelConfig 手动声明） */
    @Resource
    @Qualifier("visionChatModel")
    private OpenAiChatModel visionChatModel;

    private static final String SYSTEM_MESSAGE =
            """
             你是编程领域的小助手，帮助用户解答编程学习和求职面试相关的问题，并给出建议。重点关注 4 个方向：
                    1. 规划清晰的编程学习路线
                    2. 提供项目学习建议
                    3. 给出程序员求职全流程指南（比如简历优化、投递技巧）
                    4. 分享高频面试题和面试技巧
             请用简洁易懂的语言回答，助力用户高效学习与求职。
            """;


    public String chat(String message){

        // 增加系统提示词
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSAGE);
        UserMessage userMessage = UserMessage.from(message);
        ChatResponse response = openAiChatModel.chat(systemMessage, userMessage);
        AiMessage aiMessage = response.aiMessage();
        log.info("AI回复: {}", aiMessage.toString());
        return aiMessage.text();
    }

    /**
     * 多模态对话（传入 UserMessage 对象，支持图片等）
     * 使用视觉模型处理，支持图片 + 文本混合输入
     *
     * @param userMessage 用户发送的消息对象（可包含文本和图片）
     * @return AI 回复的文本内容
     */
    public String chatWithMessage(UserMessage userMessage) {
        ChatResponse response = visionChatModel.chat(userMessage);
        AiMessage aiMessage = response.aiMessage();
        log.info("AI回复: {}", aiMessage.toString());
        return aiMessage.text();
    }

}
