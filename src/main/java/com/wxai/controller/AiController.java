package com.wxai.controller;

import com.wxai.ai.AiCodeService;
import com.wxai.entity.AiChatMessage;
import com.wxai.entity.User;
import com.wxai.mapper.AiChatMessageMapper;
import com.wxai.service.AnswerCacheService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiCodeService aiCodeService;

    @Resource
    private AnswerCacheService answerCacheService;

    @Resource
    private AiChatMessageMapper chatMessageMapper;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatPost(@RequestParam String memoryId,
                                                  @RequestParam String message,
                                                  HttpServletRequest request) {
        if (message == null || message.isBlank() || message.length() > 2000) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("消息不能为空且长度不能超过2000字").build());
        }

        User user = (User) request.getAttribute("currentUser");
        String userId = String.valueOf(user.getId());

        if (!memoryId.startsWith(userId + "_")) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("[ERROR] 无权访问该会话").build());
        }

        saveMessage(userId, memoryId, "USER", message);

        String cached = answerCacheService.get(message);
        if (cached != null) {
            log.info("命中缓存: {}", message);
            saveMessage(userId, memoryId, "AI", cached);
            return Flux.just(
                    ServerSentEvent.<String>builder().data(cached).build(),
                    ServerSentEvent.<String>builder().data("[DONE]").build()
            );
        }

        StringBuilder fullResponse = new StringBuilder();

        return Mono.just(memoryId)
                .flatMapMany(mid -> aiCodeService.chatStream(mid, message))
                .filter(token -> token != null)
                .doOnNext(fullResponse::append)
                .map(token -> ServerSentEvent.<String>builder().data(token).build())
                .timeout(Duration.ofSeconds(180))
                .doOnComplete(() -> {
                    if (!fullResponse.isEmpty()) {
                        String response = fullResponse.toString();
                        answerCacheService.put(message, response);
                        saveMessage(userId, memoryId, "AI", response);
                    }
                })
                .onErrorResume(e -> {
                    log.error("SSE stream error: {}", e.getMessage());
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data("[ERROR] 服务暂时繁忙，请稍后重试").build());
                });
    }

    private void saveMessage(String userId, String memoryId, String role, String content) {
        try {
            AiChatMessage msg = new AiChatMessage();
            msg.setUserId(userId);
            msg.setMemoryId(memoryId);
            msg.setRole(role);
            msg.setContent(content);
            chatMessageMapper.insert(msg);
        } catch (Exception e) {
            log.error("保存消息失败: {}", e.getMessage());
        }
    }
}