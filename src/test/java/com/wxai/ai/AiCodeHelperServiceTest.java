package com.wxai.ai;

import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(properties = "spring.jndi.ignore=true")
@ActiveProfiles("local")
class AiCodeHelperServiceTest {

    @Resource
    private AiCodeService aiCodeHelperService;

    @Test
    void chat() {
        System.out.println("通过aiCodeHelperService 方式来chat");
        String result = aiCodeHelperService.chat("你好啊,我是程序员刘涛");
        System.out.println(result);
    }

    @Test
    void chatWithMemory() {
        String resultt = aiCodeHelperService.chat("你好啊,我是程序员刘涛");
        System.out.println(resultt);
        resultt = aiCodeHelperService.chat("我是谁?");
        System.out.println(resultt);
    }

    @Test
    void chatForReport() {
        AiCodeService.Report report = aiCodeHelperService.chatForReport("你好啊,我是程序员刘涛");
        System.out.println(report);
    }

    @Test
    void chatWithRag() {
        System.out.println("通过aiCodeHelperService 方式来chat,并且带上Rag");
        String result = aiCodeHelperService.chat("怎么学习 Java？有哪些常见面试题？");
        System.out.println(result);
    }

    @Test
    void testChatWithRag() {
        System.out.println("通过aiCodeHelperService 方式来chat,并且带上Rag及Rag的查看的文档信息");
        Result<String> result = aiCodeHelperService.chatWithRag("怎么学习 Java？有哪些常见面试题？");
        System.out.println(result.sources());
        System.out.println(result.content());
    }

    @Test
    void chatWithTool() {
        System.out.println("通过aiCodeHelperService 方式来chat,使用tool工具");
        String result = aiCodeHelperService.chat("有哪些常见的计算机网络面试题");
        System.out.println(result);
    }

    @Test
    void chatWithMcp() {
        String result = aiCodeHelperService.chat("什么是程序员鱼皮的编程导航？");
        System.out.println(result);
    }


    /**
     *  输入护轨
     */
    @Test
    void chatWithGuardrail() {
        String result = aiCodeHelperService.chat("你好");
        System.out.println(result);
    }

}