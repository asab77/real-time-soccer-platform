package com.example.soccerplatform.config;

import com.example.soccerplatform.dto.MatchResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.ArrayList;
import java.time.Duration;

@Configuration
public class CacheConfiguration implements CachingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfiguration.class);

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(
            ObjectMapper objectMapper,
            @Value("${app.cache.match-ttl:45s}") Duration matchCacheTtl
    ) {
        JavaType matchResponseListType = objectMapper.getTypeFactory().constructCollectionType(
                ArrayList.class,
                MatchResponse.class
        );
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, matchResponseListType);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(matchCacheTtl)
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logUnavailable("read", cache, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                logUnavailable("write", cache, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logUnavailable("eviction", cache, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logUnavailable("clear", cache, exception);
            }
        };
    }

    private void logUnavailable(String operation, Cache cache, RuntimeException exception) {
        logger.warn("Cache {} failed for cache {} ({}); continuing without cache",
                operation, cache.getName(), exception.getClass().getSimpleName());
    }
}
