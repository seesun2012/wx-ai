package com.wxai.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxai.entity.AiChatMemory;
import com.wxai.mapper.AiChatMemoryMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MySqlChatMemoryStore implements ChatMemoryStore {

    private final AiChatMemoryMapper chatMemoryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        try {
            AiChatMemory memory = chatMemoryMapper.selectByMemoryId(memoryId.toString());
            if (memory == null || memory.getMessages() == null || memory.getMessages().isBlank()) {
                return new ArrayList<>();
            }
            return deserialize(memory.getMessages());
        } catch (Exception e) {
            log.error("读取对话记忆失败 memoryId={}", memoryId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        try {
            String id = memoryId.toString();
            String json = serialize(messages);
            String title = extractTitle(messages);
            AiChatMemory memory = new AiChatMemory();
            memory.setUserId(extractUserId(id));
            memory.setMemoryId(id);
            memory.setTitle(title);
            memory.setMessages(json);
            chatMemoryMapper.upsert(memory);
        } catch (Exception e) {
            log.error("保存对话记忆失败 memoryId={}", memoryId, e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMemoryMapper.deleteByMemoryId(memoryId.toString());
    }

    private String extractTitle(List<ChatMessage> messages) {
        for (ChatMessage msg : messages) {
            if (msg instanceof UserMessage userMsg) {
                String text = userMsg.singleText();
                return text.length() > 50 ? text.substring(0, 50) + "..." : text;
            }
        }
        return "新对话";
    }

    private String extractUserId(String memoryId) {
        String[] parts = memoryId.split("_");
        return parts.length >= 2 ? parts[0] : memoryId;
    }

    private String serialize(List<ChatMessage> messages) throws Exception {
        List<MessageDTO> dtoList = new ArrayList<>();
        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage) continue;
            if (msg instanceof UserMessage userMsg) {
                dtoList.add(new MessageDTO("USER", userMsg.singleText()));
            } else if (msg instanceof AiMessage aiMsg) {
                dtoList.add(new MessageDTO("AI", aiMsg.text()));
            }
        }
        return objectMapper.writeValueAsString(dtoList);
    }

    private List<ChatMessage> deserialize(String json) throws Exception {
        List<ChatMessage> messages = new ArrayList<>();
        if (json == null || json.isBlank()) return messages;
        List<MessageDTO> dtoList = objectMapper.readValue(json, new TypeReference<>() {});
        for (MessageDTO dto : dtoList) {
            if ("USER".equals(dto.type())) {
                messages.add(UserMessage.from(dto.text()));
            } else if ("AI".equals(dto.type())) {
                if (dto.text() == null || dto.text().isBlank()) {
                    messages.add(AiMessage.from(""));
                    continue;
                }
                messages.add(AiMessage.from(dto.text()));
            }
        }
        return messages;
    }

    public record MessageDTO(String type, String text) {}
}