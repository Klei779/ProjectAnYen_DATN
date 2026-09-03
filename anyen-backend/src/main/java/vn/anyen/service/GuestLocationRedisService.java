package vn.anyen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Service for temporarily storing a guest's geolocation in Redis.
 * The key format is "anyen:guest:location:{sessionId}" and the value is a JSON
 * representation of {@link GuestLocation}.
 */
@Service
@Slf4j
public class GuestLocationRedisService {
    private static final String KEY_PREFIX = "anyen:guest:location:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public GuestLocationRedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void saveLocation(String sessionId, BigDecimal latitude, BigDecimal longitude) {
        if (sessionId == null || sessionId.isBlank()) return;
        GuestLocation location = new GuestLocation(latitude, longitude);
        try {
            String json = objectMapper.writeValueAsString(location);
            redisTemplate.opsForValue().set(KEY_PREFIX + sessionId, json, TTL);
        } catch (Exception e) {
            log.warn("Không thể lưu vị trí guest vào Redis (Redis có thể đang tắt): {}", e.getMessage());
        }
    }

    public GuestLocation getLocation(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return null;
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + sessionId);
            if (json == null) return null;
            return objectMapper.readValue(json, GuestLocation.class);
        } catch (Exception e) {
            log.warn("Không thể lấy vị trí guest từ Redis (Redis có thể đang tắt): {}", e.getMessage());
            return null;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GuestLocation {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
}
