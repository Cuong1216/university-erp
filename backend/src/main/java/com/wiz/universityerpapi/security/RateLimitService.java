package com.wiz.universityerpapi.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Rate Limiting Service sử dụng Redis Sliding Window Counter.
 *
 * <p>Không cần thư viện ngoài (Bucket4j, Resilience4j) — dùng RedisTemplate
 * sẵn có trong project để implement fixed window counter đơn giản và hiệu quả.</p>
 *
 * <p>Pattern: INCR "rate_limit:{key}:{window_bucket}" với TTL = windowSeconds</p>
 *
 * <p>Ví dụ:</p>
 * <ul>
 *   <li>Login: key = "login:192.168.1.1", max=5, window=60s</li>
 *   <li>Export: key = "export:admin_user", max=3, window=60s</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Kiểm tra và tăng counter cho request hiện tại.
     *
     * @param key            Định danh duy nhất (ví dụ: "login:{ip}", "export:{username}")
     * @param maxRequests    Số request tối đa trong cửa sổ thời gian
     * @param windowSeconds  Kích thước cửa sổ thời gian (giây)
     * @return true nếu request được phép, false nếu bị rate-limit
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key + ":" + getCurrentWindowBucket(windowSeconds);

        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) {
                log.warn("Redis trả về null khi increment key: {}", redisKey);
                return true; // Fail-open: không block nếu Redis lỗi
            }

            // Chỉ set TTL lần đầu tiên (count == 1) để tránh reset window
            if (count == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }

            boolean allowed = count <= maxRequests;
            if (!allowed) {
                log.warn("Rate limit exceeded for key: {} (count: {}/{})", key, count, maxRequests);
            }
            return allowed;

        } catch (Exception e) {
            log.error("Lỗi Redis khi kiểm tra rate limit cho key {}: {}", key, e.getMessage());
            return true; // Fail-open: không block user nếu Redis gặp sự cố
        }
    }

    /**
     * Lấy số request còn lại trong window hiện tại.
     *
     * @return số request đã dùng, hoặc 0 nếu chưa có
     */
    public long getCurrentCount(String key, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key + ":" + getCurrentWindowBucket(windowSeconds);
        try {
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value == null) return 0L;
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Tạo bucket key theo cửa sổ thời gian (fixed window).
     * Ví dụ: windowSeconds=60 → bucket theo phút hiện tại.
     */
    private long getCurrentWindowBucket(int windowSeconds) {
        return System.currentTimeMillis() / 1000 / windowSeconds;
    }
}
