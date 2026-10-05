package com.charishma.ratelimiter.core;

import com.charishma.ratelimiter.api.RateLimit.Algorithm;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class DistributedRateLimiter {
    private final Ignite ignite;
    private final IgniteCache<String, State> states;

    public DistributedRateLimiter(Ignite ignite) {
        this.ignite = ignite;
        CacheConfiguration<String, State> cfg = new CacheConfiguration<>("rate-limit-state");
        cfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);
        this.states = ignite.getOrCreateCache(cfg);
    }

    public synchronized RateLimitResult check(String key, long limit, long windowSeconds, Algorithm algorithm) {
        long now = System.currentTimeMillis();
        State current = states.get(key);
        State next = current == null ? new State() : current.copy();
        boolean allowed;
        if (algorithm == Algorithm.SLIDING_WINDOW) {
            allowed = slidingWindow(next, limit, windowSeconds, now);
        } else {
            allowed = tokenBucket(next, limit, windowSeconds, now);
        }
        states.put(key, next);
        return new RateLimitResult(allowed, next.remaining(limit), allowed ? 0 : Math.max(1, windowSeconds));
    }

    private boolean tokenBucket(State s, long limit, long window, long now) {
        double refillPerMs = (double) limit / (window * 1000d);
        if (s.lastRefill > 0) {
            s.tokens = Math.min(limit, s.tokens + (now - s.lastRefill) * refillPerMs);
        } else {
            s.tokens = limit;
        }
        s.lastRefill = now;
        if (s.tokens >= 1) { s.tokens -= 1; return true; }
        return false;
    }

    private boolean slidingWindow(State s, long limit, long window, long now) {
        long cutoff = now - window * 1000;
        while (!s.timestamps.isEmpty() && s.timestamps.peekFirst() < cutoff) s.timestamps.removeFirst();
        if (s.timestamps.size() >= limit) return false;
        s.timestamps.addLast(now);
        return true;
    }

    public static class State implements java.io.Serializable {
        double tokens;
        long lastRefill;
        Deque<Long> timestamps = new ArrayDeque<>();
        State copy() {
            State x = new State(); x.tokens=tokens; x.lastRefill=lastRefill; x.timestamps=new ArrayDeque<>(timestamps); return x;
        }
        long remaining(long limit) {
            return Math.max(0, Math.min(limit, Math.round(tokens)));
        }
    }
}