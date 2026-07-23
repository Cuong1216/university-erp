package com.wiz.universityerpapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiz.universityerpapi.security.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * HTTP Filter thực thi Rate Limiting cho các endpoint nhạy cảm.
 *
 * <p>Rules được áp dụng:</p>
 * <ul>
 *   <li><b>POST /api/v1/auth/login</b>: Tối đa 5 requests/60 giây per IP
 *       — Bảo vệ chống brute-force password</li>
 *   <li><b>GET /api/v1/luong/export/excel</b>: Tối đa 3 requests/60 giây per authenticated user
 *       — Bảo vệ chống abuse vì export Excel là thao tác CPU nặng</li>
 * </ul>
 *
 * <p>Response khi bị rate-limit: HTTP 429 với header "Retry-After: 60"</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    // Login: 5 attempts / 60 giây per IP
    private static final int LOGIN_MAX_REQUESTS = 5;
    private static final int LOGIN_WINDOW_SECONDS = 60;

    // Export: 3 requests / 60 giây per user
    private static final int EXPORT_MAX_REQUESTS = 3;
    private static final int EXPORT_WINDOW_SECONDS = 60;

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Rule 1: Rate limit login endpoint by IP
        if ("POST".equalsIgnoreCase(method) && path.equals("/api/v1/auth/login")) {
            String clientIp = getClientIp(request);
            String key = "login:" + clientIp;

            if (!rateLimitService.isAllowed(key, LOGIN_MAX_REQUESTS, LOGIN_WINDOW_SECONDS)) {
                sendRateLimitResponse(response, LOGIN_WINDOW_SECONDS,
                    "Quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau " + LOGIN_WINDOW_SECONDS + " giây.");
                return;
            }
        }

        // Rule 2: Rate limit Excel export endpoint by username (từ JWT)
        if ("GET".equalsIgnoreCase(method) && path.equals("/api/v1/luong/export/excel")) {
            // Lấy username từ request attribute (được set bởi JwtAuthenticationFilter)
            String principal = request.getUserPrincipal() != null
                    ? request.getUserPrincipal().getName()
                    : getClientIp(request); // Fallback về IP nếu chưa auth
            String key = "export:" + principal;

            if (!rateLimitService.isAllowed(key, EXPORT_MAX_REQUESTS, EXPORT_WINDOW_SECONDS)) {
                sendRateLimitResponse(response, EXPORT_WINDOW_SECONDS,
                    "Bạn đã xuất Excel quá nhiều lần. Tối đa " + EXPORT_MAX_REQUESTS + " lần/phút.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Trả về HTTP 429 với body JSON và header Retry-After.
     */
    private void sendRateLimitResponse(HttpServletResponse response, int retryAfterSeconds, String message)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-RateLimit-Limit", String.valueOf(retryAfterSeconds));

        Map<String, Object> body = Map.of(
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "error", "Too Many Requests",
                "message", message,
                "retryAfter", retryAfterSeconds
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    /**
     * Lấy IP thực của client, hỗ trợ reverse proxy (Nginx, CloudFlare).
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Lấy IP đầu tiên trong chuỗi (IP thực của client)
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
