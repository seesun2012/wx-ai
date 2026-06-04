package com.wxai.ai.listener;


import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ChatModelListenerConfig {

    /** 可观测性
     * 可以通过自定义 Listener 获取 ChatModel 的调用信息
     * 创建并配置一个ChatModelListener Bean
     * 该Bean用于监听和处理聊天模型的请求、响应和错误事件
     *
     * @return ChatModelListener 配置好的聊天模型监听器实例
     */
    @Bean
    public ChatModelListener chatModelListener(){

    // 返回一个匿名的ChatModelListener实现类实例
        return new ChatModelListener() {


        /**
         * 处理聊天模型请求事件
         * 当有新的聊天请求时，会调用此方法记录请求信息
         *
         * @param requestContext 包含聊天请求上下文信息
         */
            @Override
            public void onRequest(ChatModelRequestContext requestContext) {
                log.info("onRequest(): {}", requestContext.chatRequest());
            }

        /**
         * 处理聊天模型响应事件
         * 当聊天模型返回响应时，会调用此方法记录响应信息
         *
         * @param responseContext 包含聊天响应上下文信息
         */
            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                log.info("onResponse(): {}", responseContext.chatResponse());
            }

        /**
         * 处理聊天模型错误事件
         * 当聊天模型处理过程中发生错误时，会调用此方法记录错误信息
         *
         * @param errorContext 包含错误上下文信息
         */
            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.info("onError(): {}", errorContext.error().getMessage());
            }
        };


    }

}
