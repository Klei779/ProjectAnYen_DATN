package vn.anyen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.anyen.dto.response.TinNhanTuVanResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatRedisService {

    private static final Logger log = LoggerFactory.getLogger(ChatRedisService.class);
    private static final String MESSAGE_KEY_PREFIX = "anyen:chat:messages:";
    private static final String GUEST_KEY_PREFIX = "anyen:chat:guest:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    private static final long REDIS_RETRY_DELAY_MS = 30_000L;

    private final AtomicBoolean redisDownLogged = new AtomicBoolean(false);

    private volatile long redisRetryAfter = 0L;
    public ChatRedisService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${chat.redis.ttl-hours:48}") long ttlHours
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofHours(Math.max(1, ttlHours));
    }

    public void rememberGuestSession(String tokenId, String tokenPhien) {
        if (isBlank(tokenId) || isBlank(tokenPhien) || !canUseRedis()) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(
                    guestKey(tokenId),
                    tokenPhien,
                    ttl
            );

            markRedisAvailable();
        } catch (Exception e) {
            markRedisDown(e);
        }
    }

    /**
     * Redis là lớp hỗ trợ thu hồi/cache, JWT vẫn tự xác thực được khi Redis tạm ngắt.
     * Nếu Redis có bản ghi thì token phải khớp đúng phiên.
     */
    public boolean isGuestSessionAllowed(String tokenId, String tokenPhien) {
        if (isBlank(tokenId) || isBlank(tokenPhien)) {
            return false;
        }

        // Redis hỏng thì vẫn cho JWT tự xác thực.
        if (!canUseRedis()) {
            return true;
        }

        try {
            String mappedSession =
                    redisTemplate.opsForValue().get(guestKey(tokenId));

            markRedisAvailable();

            return mappedSession == null
                    || mappedSession.equals(tokenPhien);
        } catch (Exception e) {
            markRedisDown(e);
            return true;
        }
    }

    public List<TinNhanTuVanResponse> getCachedMessages(String tokenPhien) {
        if (isBlank(tokenPhien) || !canUseRedis()) {
            return List.of();
        }

        try {
            String key = messageKey(tokenPhien);

            List<String> values = redisTemplate.opsForList().range(
                    key,
                    0,
                    -1
            );

            markRedisAvailable();

            if (values == null || values.isEmpty()) {
                return List.of();
            }

            List<TinNhanTuVanResponse> messages =
                    new ArrayList<>(values.size());

            for (String value : values) {
                JsonNode node = objectMapper.readTree(value);

                messages.add(new TinNhanTuVanResponse(
                        longValue(node, "maTinNhan"),
                        longValue(node, "maPhien"),
                        textValue(node, "nguoiGui"),
                        integerValue(node, "maNhanVien"),
                        textValue(node, "tenNguoiGui"),
                        textValue(node, "noiDung"),
                        booleanValue(node, "daDoc"),
                        dateTimeValue(node, "createdAt")
                ));
            }

            redisTemplate.expire(key, ttl);

            return messages;
        } catch (Exception e) {
            markRedisDown(e);

            // Không gọi evictMessages() ở đây.
            // Gọi tiếp sẽ gây thêm một warning nữa khi Redis đang hỏng.
            return List.of();
        }
    }

    public void cacheMessages(
            String tokenPhien,
            List<TinNhanTuVanResponse> messages
    ) {
        if (
                isBlank(tokenPhien)
                        || messages == null
                        || !canUseRedis()
        ) {
            return;
        }

        try {
            String key = messageKey(tokenPhien);

            redisTemplate.delete(key);

            if (!messages.isEmpty()) {
                List<String> values = messages.stream()
                        .map(this::toJson)
                        .toList();

                redisTemplate.opsForList().rightPushAll(key, values);
                redisTemplate.expire(key, ttl);
            }

            markRedisAvailable();
        } catch (Exception e) {
            markRedisDown(e);
        }
    }
    public void evictMessages(String tokenPhien) {
        if (isBlank(tokenPhien) || !canUseRedis()) {
            return;
        }

        try {
            redisTemplate.delete(messageKey(tokenPhien));
            markRedisAvailable();
        } catch (Exception e) {
            markRedisDown(e);
        }
    }

    private String toJson(TinNhanTuVanResponse message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể chuyển tin nhắn sang JSON", e);
        }
    }

    private Long longValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asLong();
    }

    private Integer integerValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private Boolean booleanValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private LocalDateTime dateTimeValue(JsonNode node, String field) {
        String value = textValue(node, field);
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private String messageKey(String tokenPhien) {
        return MESSAGE_KEY_PREFIX + tokenPhien;
    }

    private String guestKey(String tokenId) {
        return GUEST_KEY_PREFIX + tokenId;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
    private boolean canUseRedis() {
        return System.currentTimeMillis() >= redisRetryAfter;
    }

    private void markRedisDown(Exception exception) {
        redisRetryAfter = System.currentTimeMillis() + REDIS_RETRY_DELAY_MS;

        // Chỉ log một lần khi Redis vừa mất kết nối.
        if (redisDownLogged.compareAndSet(false, true)) {
            log.warn(
                    "Redis không kết nối được. Tạm bỏ qua Redis trong 30 giây, hệ thống tiếp tục dùng MySQL: {}",
                    exception.getMessage()
            );
        }
    }

    private void markRedisAvailable() {
        redisRetryAfter = 0L;

        if (redisDownLogged.compareAndSet(true, false)) {
            log.info("Redis đã kết nối lại thành công.");
        }
    }
}
