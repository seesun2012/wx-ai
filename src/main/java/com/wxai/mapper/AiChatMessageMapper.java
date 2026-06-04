package com.wxai.mapper;

import com.wxai.entity.AiChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AiChatMessageMapper {

    @Insert("INSERT INTO ai_chat_message(user_id, memory_id, role, content) " +
            "VALUES (#{userId}, #{memoryId}, #{role}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiChatMessage message);

    @Select("SELECT * FROM ai_chat_message WHERE memory_id = #{memoryId} " +
            "AND user_id = #{userId} AND (#{lastId} = 0 OR id < #{lastId}) " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<AiChatMessage> selectPage(@Param("memoryId") String memoryId,
                                   @Param("userId") String userId,
                                   @Param("lastId") long lastId,
                                   @Param("limit") int limit);

    @Delete("DELETE FROM ai_chat_message WHERE memory_id = #{memoryId}")
    void deleteByMemoryId(@Param("memoryId") String memoryId);
}