package com.wxai.mapper;

import com.wxai.entity.User;
import org.apache.ibatis.annotations.*;

public interface UserMapper {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE token = #{token}")
    User selectByToken(@Param("token") String token);

    @Insert("INSERT INTO sys_user(username, password, nickname) VALUES (#{username}, #{password}, #{nickname})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE sys_user SET token = #{token} WHERE id = #{id}")
    void updateToken(@Param("id") Long id, @Param("token") String token);

    @Update("UPDATE sys_user SET token = NULL WHERE id = #{id}")
    void clearToken(@Param("id") Long id);
}