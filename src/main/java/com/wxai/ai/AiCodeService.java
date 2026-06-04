package com.wxai.ai;

import com.wxai.ai.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *
 * SSE接口化
 * AI代码助手服务接口
 * 该接口定义了AI代码助手的基本功能，主要提供与AI进行对话交互的能力
 * 通过给 AI Service 加上 @AiService 注解，就能自动创建出服务实例了,记得注释掉之前工厂类的 @Configuration 注解，否则会出现 Bean 冲突
 */
//@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel")
@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeService {


    /**
     * 与AI进行对话的方法，返回一个流式响应
     *
     * @param memoryId   会话ID
     * @param userMessage 用户输入的消息内容
     * @return AI返回的响应内容流
     */
    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId String memoryId, @UserMessage String userMessage);



    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(String userMessage);


    /**
     * 与AI进行对话的方法
     *
     * @param userMessage 用户输入的消息内容
     * @return AI返回的响应内容
     *
     * @SystemMessage 注解表示系统消息配置，从指定的资源文件"system-prompt.txt"中加载系统提示信息
     */
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String userMessage);


    /**
     * 结构化输出有 3 种实现方式：
     *
     * 利用大模型的 JSON schema
     * 利用 Prompt + JSON Mode
     * 利用 Prompt
     * @param message
     * @return
     */
    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(String message);

    // 学习报告 (相当于DTO)
    record Report(String name, List<String> suggestList){}

    /**
     *默认是 Prompt 模式，也就是在原本的用户提示词下 拼接一段内容 来指定大模型强制输出包含特定字段的 JSON 文本。
     * 你是一个专业的信息提取助手。请从给定文本中提取人员信息，
     * 并严格按照以下 JSON 格式返回结果：
     *
     * {
     *     "name": "人员姓名",
     *     "age": 年龄数字,
     *     "height": 身高（米），
     *     "married": true/false,
     *     "occupation": "职业"
     * }
     *
     * 重要规则：
     * 1. 只返回 JSON 格式，不要添加任何解释
     * 2. 如果信息不明确，使用 null
     * 3. age 必须是数字，不是字符串
     * 4. married 必须是布尔值
     *
     *
     */

}
