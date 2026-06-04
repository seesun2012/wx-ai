package com.wxai.controller;

import com.wxai.entity.AiChatMemory;
import com.wxai.entity.AiChatMessage;
import com.wxai.entity.User;
import com.wxai.mapper.AiChatMemoryMapper;
import com.wxai.mapper.AiChatMessageMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Resource
    private AiChatMemoryMapper chatMemoryMapper;

    @Resource
    private AiChatMessageMapper chatMessageMapper;

    @PostMapping("/create")
    public Map<String, Object> create(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        String userId = String.valueOf(user.getId());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String memoryId = userId + "_" + uuid;

        AiChatMemory memory = new AiChatMemory();
        memory.setUserId(userId);
        memory.setMemoryId(memoryId);
        memory.setTitle("新对话");
        chatMemoryMapper.create(memory);

        Map<String, Object> data = new HashMap<>();
        data.put("memoryId", memoryId);
        data.put("title", "新对话");
        data.put("createdAt", memory.getCreatedAt());
        data.put("updatedAt", memory.getUpdatedAt());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> list(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        List<AiChatMemory> sessions = chatMemoryMapper.selectByUserId(String.valueOf(user.getId()));
        List<Map<String, Object>> data = sessions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("memoryId", s.getMemoryId());
            map.put("title", s.getTitle() != null ? s.getTitle() : "新对话");
            map.put("createdAt", s.getCreatedAt());
            map.put("updatedAt", s.getUpdatedAt());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    @PutMapping("/{memoryId}/title")
    public Map<String, Object> rename(@PathVariable String memoryId,
                                      @RequestBody Map<String, String> body,
                                      HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        String userId = String.valueOf(user.getId());

        if (!memoryId.startsWith(userId + "_")) {
            return errorResponse(403, "无权操作该会话");
        }

        String title = body.get("title");
        if (title == null || title.isBlank() || title.length() > 30) {
            return errorResponse(400, "标题不能为空且不超过30字");
        }

        int rows = chatMemoryMapper.updateTitle(memoryId, userId, title.trim());
        if (rows == 0) {
            return errorResponse(404, "会话不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "修改成功");
        return result;
    }

    @GetMapping("/{memoryId}/messages")
    public Map<String, Object> messages(@PathVariable String memoryId,
                                        @RequestParam(defaultValue = "0") long lastId,
                                        @RequestParam(defaultValue = "20") int limit,
                                        HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        String userId = String.valueOf(user.getId());

        if (!memoryId.startsWith(userId + "_")) {
            return errorResponse(403, "无权访问该会话");
        }

        List<AiChatMessage> msgs = chatMessageMapper.selectPage(memoryId, userId, lastId, limit);
        boolean hasMore = msgs.size() == limit;

        Collections.reverse(msgs);

        List<Map<String, Object>> data = msgs.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("role", "USER".equals(m.getRole()) ? "user" : "assistant");
            map.put("content", m.getContent());
            map.put("createdAt", m.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("messages", data);
        pageData.put("hasMore", hasMore);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", pageData);
        return result;
    }

    @DeleteMapping("/{memoryId}")
    public Map<String, Object> delete(@PathVariable String memoryId,
                                      HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        String userId = String.valueOf(user.getId());

        if (!memoryId.startsWith(userId + "_")) {
            return errorResponse(403, "无权操作该会话");
        }

        chatMessageMapper.deleteByMemoryId(memoryId);
        chatMemoryMapper.deleteByMemoryId(memoryId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }

    private Map<String, Object> errorResponse(int code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        return result;
    }
}