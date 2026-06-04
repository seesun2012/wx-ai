package com.wxai.mapper;

import com.wxai.entity.AiChatMemory;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AiChatMemoryMapper {

    @Select("SELECT * FROM ai_chat_memory WHERE memory_id = #{memoryId}")
    AiChatMemory selectByMemoryId(@Param("memoryId") String memoryId);

    @Select("SELECT id, user_id, memory_id, title, created_at, updated_at FROM ai_chat_memory " +
            "WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<AiChatMemory> selectByUserId(@Param("userId") String userId);

    @Insert("INSERT INTO ai_chat_memory(user_id, memory_id, title) VALUES (#{userId}, #{memoryId}, #{title})")
    void create(AiChatMemory memory);

    @Update("UPDATE ai_chat_memory SET title = #{title}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE memory_id = #{memoryId} AND user_id = #{userId}")
    int updateTitle(@Param("memoryId") String memoryId, @Param("userId") String userId, @Param("title") String title);

    @Insert("INSERT INTO ai_chat_memory(user_id, memory_id, title, messages) " +
            "VALUES (#{userId}, #{memoryId}, #{title}, #{messages}) " +
            "ON DUPLICATE KEY UPDATE messages = #{messages}, " +
            "title = CASE WHEN title IS NULL OR title = '新对话' THEN #{title} ELSE title END, " +
            "updated_at = CURRENT_TIMESTAMP")
    void upsert(AiChatMemory memory);

    @Delete("DELETE FROM ai_chat_memory WHERE memory_id = #{memoryId}")
    void deleteByMemoryId(@Param("memoryId") String memoryId);

    @Delete("DELETE FROM ai_chat_memory WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") String userId);
}