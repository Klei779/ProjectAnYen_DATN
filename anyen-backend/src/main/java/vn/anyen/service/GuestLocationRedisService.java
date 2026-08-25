package vn.anyen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
        GuestLocation location = new GuestLocation(latitude, longitude);
        try {
            String json = objectMapper.writeValueAsString(location);
            redisTemplate.opsForValue().set(KEY_PREFIX + sessionId, json, TTL);
        } catch (JsonProcessingException e) {

        }
    }

    public GuestLocation getLocation(String sessionId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + sessionId);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, GuestLocation.class);
        } catch (JsonProcessingException e) {
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
