package com.wims.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = "com.wims.backend.repository")
public class RedisConfig {

    // 1. Cấu hình RedisTemplate (Để thao tác thủ công sau này nếu cần)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key là String
        template.setKeySerializer(new StringRedisSerializer());
        // Value là JSON (Dùng thư viện Jackson có sẵn trong Spring Web)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    // 2. Cấu hình RedisCacheManager (Để @Cacheable hoạt động với JSON)
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 1. Cấu hình mặc định cho tất cả các cache
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 2. Cấu hình riêng cho từng loại (Map)
        Map<String, RedisCacheConfiguration> specificConfig = new HashMap<>();

        // Cache "product_search" chỉ sống 1 phút (Để danh sách sản phẩm luôn tươi mới)
        specificConfig.put("product_search", defaultConfig.entryTtl(Duration.ofMinutes(1)));

        // Cache "product_detail" sống 1 ngày (Vì chi tiết ít khi đổi)
        specificConfig.put("product_detail", defaultConfig.entryTtl(Duration.ofDays(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig) // Áp dụng default
                .withInitialCacheConfigurations(specificConfig) // Áp dụng config riêng
                .build();
    }
}