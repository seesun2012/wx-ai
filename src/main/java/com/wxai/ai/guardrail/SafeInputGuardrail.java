package com.wxai.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Set;
import java.util.regex.Pattern;

public class SafeInputGuardrail implements InputGuardrail {

    private static final int MAX_INPUT_LENGTH = 2000;

    private static final Set<String> SENSITIVE_WORDS = Set.of(
            "kill", "evil", "hack", "attack",
            "自杀", "杀人", "毒品", "赌博", "色情", "暴力"
    );

    private static final Pattern INJECTION_PATTERN = Pattern.compile(
            "(?i)(ignore previous|ignore above|disregard|forget your instructions|你现在是|你忘记你的指令|请扮演)"
    );

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {

        String userInput = userMessage.singleText();

        if (userInput.length() > MAX_INPUT_LENGTH) {
            return fatal("输入过长，最大" + MAX_INPUT_LENGTH + "字符");
        }

        String lowerInput = userInput.toLowerCase();

        String[] words = lowerInput.split("[\\W\\u4e00-\\u9fa5]+");
        for (String word : words) {
            if (SENSITIVE_WORDS.contains(word)) {
                return fatal("包含敏感词: " + word);
            }
        }

        for (String sensitive : SENSITIVE_WORDS) {
            if (lowerInput.contains(sensitive)) {
                return fatal("包含敏感内容");
            }
        }

        if (INJECTION_PATTERN.matcher(userInput).find()) {
            return fatal("检测到提示注入攻击");
        }

        return success();
    }
}