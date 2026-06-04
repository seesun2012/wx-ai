package com.wxai;

import com.wxai.ai.AiCode;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiCodeHelperTest {

    @Resource
    private AiCode aiCodeHelper;

    @Test
    void chat() {
        aiCodeHelper.chat("你好,我是程序员刘涛");
    }

    /**
     * 多模态 - Multimodality（LangChain4j 方式，直接传图片 URL）
     * 使用 GitHub CDN 上的图片，硅基流动服务端可正常访问
     */
    @Test
    void chatWithMessage() {
        // GitHub avatars CDN 可被硅基流动服务端访问
        String imageUrl = "https://avatars.githubusercontent.com/u/14985020?v=4";

        UserMessage userMessage = UserMessage.from(
                TextContent.from("描述这张图片"),
                ImageContent.from(imageUrl)
        );
        aiCodeHelper.chatWithMessage(userMessage);
    }
}
