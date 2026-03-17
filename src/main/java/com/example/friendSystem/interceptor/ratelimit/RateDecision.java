package com.example.friendSystem.interceptor.ratelimit;

/**
 * 인터셉터의 헤더 응답을 위한 결과 클래스입니다.
 * @param allowed 허용여부
 * @param remaining 남은 횟수
 * @param retryInNanoSec 다음 허용 호출 가능까지 남은 시간(0이면 즉시 호출 가능)
 */
public record RateDecision(boolean allowed, int remaining, long retryInNanoSec) {

    public static RateDecision allowed(int remaining) {
        return new RateDecision(true, remaining, 0L);
    }

    public static RateDecision blocked(long retryInNanoSec) {
        return new RateDecision(false, 0, retryInNanoSec);
    }

    public long retryAfterSecondsCeil() {
        return (long) Math.ceil(retryInNanoSec / 1_000_000_000.0);
    }

    public long retryAfterMillis() {
        return Math.max(0, retryInNanoSec / 1_000_000);
    }

}
