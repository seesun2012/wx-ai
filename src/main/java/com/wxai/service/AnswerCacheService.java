package com.wxai.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class AnswerCacheService {

    private final Cache<String, String> answerCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofHours(6))
            .recordStats()
            .build();

    public String get(String question) {
        return answerCache.getIfPresent(normalize(question));
    }

    public void put(String question, String answer) {
        answerCache.put(normalize(question), answer);
    }

    private String normalize(String question) {
        return question.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}