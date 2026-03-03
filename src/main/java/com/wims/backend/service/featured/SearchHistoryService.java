package com.wims.backend.service.featured;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    // Dùng StringRedisTemplate để đảm bảo lưu và lấy đều là chuỗi text thuần
    private final RedisTemplate<String, String> redisTemplate;

    // Key ví dụ: "search_history:user:1"
    private String getKey(Long userId) {
        return "search_history:" + userId;
    }

    public void saveSearchHistory(Long userId, String keyword) {
        String key = getKey(userId);

        // 1. Xóa từ khóa cũ nếu trùng (để đưa cái mới lên đầu)
        redisTemplate.opsForList().remove(key, 0, keyword);

        // 2. Đưa từ khóa mới vào đầu danh sách (Left Push)
        redisTemplate.opsForList().leftPush(key, keyword);

        // 3. (Tùy chọn) Chỉ giữ lại 5 từ khóa gần nhất
        redisTemplate.opsForList().trim(key, 0, 4);
    }

    public List<String> getSearchHistory(Long userId) {
        String key = getKey(userId);
        // Lấy tất cả
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public void removeKeyword(Long userId, String keyword) {
        String key = getKey(userId);
        // Lấy tất cả
        redisTemplate.opsForList().remove(key, 1, keyword);
    }
}
