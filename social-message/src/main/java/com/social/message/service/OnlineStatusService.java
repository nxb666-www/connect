package com.social.message.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OnlineStatusService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:active:";
    private static final long ONLINE_THRESHOLD_MINUTES = 5;

    public void updateLastActive(Long userId) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + userId,
                String.valueOf(System.currentTimeMillis()),
                10, TimeUnit.MINUTES
        );
    }

    public boolean isOnline(Long userId) {
        String val = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        if (val == null) return false;
        long lastActive = Long.parseLong(val);
        return (System.currentTimeMillis() - lastActive) < ONLINE_THRESHOLD_MINUTES * 60 * 1000;
    }

    public Map<Long, Boolean> batchCheckOnline(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();

        List<String> keys = userIds.stream()
                .map(id -> KEY_PREFIX + id)
                .toList();
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, Boolean> result = new HashMap<>();
        long threshold = System.currentTimeMillis() - ONLINE_THRESHOLD_MINUTES * 60 * 1000;
        for (int i = 0; i < userIds.size(); i++) {
            String val = (values != null && i < values.size()) ? values.get(i) : null;
            boolean online = false;
            if (val != null) {
                try {
                    online = Long.parseLong(val) >= threshold;
                } catch (NumberFormatException ignored) {}
            }
            result.put(userIds.get(i), online);
        }
        return result;
    }
}
