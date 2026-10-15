package in.jecrcu.placement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory sliding-window rate limiter for logins, submissions, and comments (SPM-2).
 */
@Service
@Slf4j
public class RateLimitService {

    private final Map<String, ConcurrentLinkedDeque<Instant>> requestBuckets = new ConcurrentHashMap<>();

    /**
     * Checks and consumes a token if limit has not been exceeded.
     *
     * @param key unique identifier (e.g. IP address or userId)
     * @param maxRequests maximum allowed requests within window
     * @param windowSeconds duration of sliding window in seconds
     * @return true if allowed, false if rate limited
     */
    public boolean tryAcquire(String key, int maxRequests, long windowSeconds) {
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(windowSeconds);

        ConcurrentLinkedDeque<Instant> timestamps = requestBuckets.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // Evict expired timestamps
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }

        log.warn("Rate limit exceeded for action key: {}", key);
        return false;
    }

    public boolean isLoginAllowed(String clientIp) {
        // Max 5 attempts per 60 seconds
        return tryAcquire("login:" + clientIp, 5, 60);
    }

    public boolean isSubmissionAllowed(Long userId) {
        // Max 5 experience submissions per hour
        return tryAcquire("submit:" + userId, 5, 3600);
    }

    public boolean isCommentAllowed(Long userId) {
        // Max 10 comments per 5 minutes
        return tryAcquire("comment:" + userId, 10, 300);
    }
}
