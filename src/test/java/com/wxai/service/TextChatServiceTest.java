package com.wxai.service;

import com.wxai.WxAiApplication;
import com.wxai.memory.MySqlChatMemoryStore;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = WxAiApplication.class, properties = "spring.jndi.ignore=true")
class TextChatServiceTest {

    @Resource
    private TextChatService textChatService;

    @Resource
    private MySqlChatMemoryStore mySqlChatMemoryStore;


    @Resource
    private TextChatWithMemoryService textChatWithMemoryService;


    @BeforeEach
    void setUp() {
        // 每次测试前清理 userId=1 的历史记忆，避免脏数据干扰
        mySqlChatMemoryStore.deleteMessages("2");
    }

    @Test
    void chat() {
        System.out.println("第一轮USER输入:你好,我是程序员刘涛");
        String res = textChatService.chat("1", "你好,我是程序员刘涛");
        System.out.println("=====================================");
        System.out.println("第一轮AI回复：");
        System.out.println(res);
        System.out.println();

        System.out.println("第二轮USER输入:我是谁");
        res = textChatService.chat("1", "我是谁");
        System.out.println("=====================================");
        System.out.println("第二轮AI回复：");
        System.out.println(res);
    }

    @Test
    void textChatWithMemoryService() {
        System.out.println("第一轮USER输入:你好,我是程序员刘涛");
        String res = textChatWithMemoryService.chat("2", "你好,我是程序员刘涛");
        System.out.println("=====================================");
        System.out.println("第一轮AI回复：");
        System.out.println(res);
        System.out.println();

        System.out.println("第二轮USER输入:根据我们刚才的对话，我是谁？");
        res = textChatWithMemoryService.chat("2", "根据我们刚才的对话，我是谁？");
        System.out.println("=====================================");
        System.out.println("第二轮AI回复：");
        System.out.println(res);
    }
}