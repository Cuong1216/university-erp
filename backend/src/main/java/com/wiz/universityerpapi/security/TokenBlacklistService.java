package com.wiz.universityerpapi.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service quản lý Token Blacklist bằng Redis.
 *
 * <p>Giải quyết vấn đề của JWT stateless: khi user logout hoặc bị deactivate,
 * token cũ vẫn valid đến hết thời hạn. Blacklist lưu JTI (JWT ID) trong Redis
 * với TTL = thời gian còn lại của token để tự động dọn dẹp.</p>
 *
 * <p>Pattern: SET "blacklist:{jti}" "1" EX {remaining_seconds}</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Đưa token vào blacklist.
     *
     * @param jti              JWT ID của token cần revoke
     * @param remainingSeconds Thời gian còn lại (giây) trước khi token hết hạn
     */
    public void blacklist(String jti, long remainingSeconds) {
        if (remainingSeconds <= 0) {
            log.debug("Token JTI {} đã hết hạn, không cần blacklist", jti);
            return;
        }
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(remainingSeconds));
        log.info("Token JTI {} đã được blacklist, TTL: {}s", jti, remainingSeconds);
    }

    /**
     * Kiểm tra token có trong blacklist không.
     *
     * @param jti JWT ID cần kiểm tra
     * @return true nếu token đã bị revoke
     */
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}
