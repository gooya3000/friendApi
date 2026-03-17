package com.example.friendSystem.interceptor.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * X-user-id + HTTP method + request URI 조합을 기준으로 초당 최대 10회 요청을 허용하는 인터셉터입니다.
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int LIMIT_PER_SECOND = 10; // 초당 10회 제한
    private final RateLimiter limiter = new RateLimiter(LIMIT_PER_SECOND);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 친구 신청 요청은 POST 이므로 한 번 더 체크
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 헤더의 사용자 ID 유효성 체크
        String xUserId = request.getHeader("X-user-id");
        if (xUserId == null) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing required header: X-user-id");
            return false;
        }
        long userId;
        try {
            userId = Long.parseLong(xUserId);
        } catch (NumberFormatException e) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid X-user-id");
            return false;
        }

        // 횟수 체크 기준 키 조합: userId + METHOD + URI
        final String key = userId + ":" + request.getMethod() + ":" + request.getRequestURI();

        // 횟수 제한 여부 체크
        long nowNs = System.nanoTime();
        RateDecision result = limiter.tryRequest(key, nowNs);

        // 요청 횟수 체크 힌트 헤더
        response.setHeader("X-RateLimit-Limit", String.valueOf(LIMIT_PER_SECOND)); // 제한 횟수
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, result.remaining()))); // 남은 횟수
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.retryAfterMillis())); // 초기화 되는 시간

        // 횟수 초과 시 429 반환
        if (!result.allowed()) {
            response.setHeader("Retry-After", String.valueOf(Math.max(1, result.retryAfterSecondsCeil()))); // s
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests");
            return false;
        }

        return true;
    }
}
