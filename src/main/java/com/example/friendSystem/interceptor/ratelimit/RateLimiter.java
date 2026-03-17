package com.example.friendSystem.interceptor.ratelimit;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 1초 동안의 요청 기록을 유지하며 그 수에 따라 허용하거나 거절하는 리미터입니다.
 */
public final class RateLimiter {

    private final long windowNanoSec = 1_000_000_000L; // 1초(나노초)
    private final int limit;                       // 초당 허용 횟수
    private final ConcurrentHashMap<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int limitPerSecond) {
        this.limit = limitPerSecond;
    }

    /**
     * 요청 시도 체크
     * @param key   "userId:METHOD:URI" 조합 키
     * @param nowNanoSec 현재의 나노 초
     * @return 허용 여부, 남은 횟수, 재시도까지 남은 시간
     */
    public RateDecision tryRequest(String key, long nowNanoSec) {
        Deque<Long> deque = buckets.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // 1초 이전의 오래된 요청 시각(타임스탬프) 확인하여 제거
        long before1Sec = nowNanoSec - windowNanoSec;
        while (true) {
            Long firstRequestTime = deque.peekFirst();
            if (firstRequestTime == null || firstRequestTime >= before1Sec) break;
            deque.pollFirst();
        }

        // 디큐에 저장된 사이즈가 허용 횟수를 넘어가면 거절
        if (deque.size() >= limit) {

            long firstRequestTime = deque.peekFirst() != null ? deque.peekFirst() : -1;
            if (firstRequestTime == -1) RateDecision.blocked(windowNanoSec);
            long retryTime = (firstRequestTime + windowNanoSec) - nowNanoSec; // 다음 허용까지 남은 시간 계산
            return RateDecision.blocked(retryTime);

        }

        // 디큐에 현재 요청 시각 저장 후 허용
        deque.addLast(nowNanoSec);
        return RateDecision.allowed(limit - deque.size());
    }

}
