package com.wxai.config;

import com.wxai.mapper.AiChatMemoryMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    public AiChatMemoryMapper aiChatMemoryMapper() {
        AiChatMemoryMapper mockMapper = Mockito.mock(AiChatMemoryMapper.class);
        when(mockMapper.selectByMemoryId(any())).thenReturn(null);
        return mockMapper;
    }


//
//    @Bean
//    @Primary
//    public MySqlChatMemoryStore mySqlChatMemoryStore(AiChatMemoryMapper aiChatMemoryMapper) {
//        return new MySqlChatMemoryStore(aiChatMemoryMapper, new ObjectMapper());
//    }
}
